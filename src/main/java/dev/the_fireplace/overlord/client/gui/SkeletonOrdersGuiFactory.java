package dev.the_fireplace.overlord.client.gui;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import dev.the_fireplace.lib.api.chat.interfaces.Translator;
import dev.the_fireplace.lib.api.client.injectables.ConfigScreenBuilderFactory;
import dev.the_fireplace.lib.api.client.interfaces.ConfigScreenBuilder;
import dev.the_fireplace.lib.api.client.interfaces.CustomButtonBuilder;
import dev.the_fireplace.lib.api.client.interfaces.OptionBuilder;
import dev.the_fireplace.overlord.Overlord;
import dev.the_fireplace.overlord.client.gui.config.PositionSelectorGui;
import dev.the_fireplace.overlord.client.gui.config.listbuilder.ListBuilderGui;
import dev.the_fireplace.overlord.domain.client.OrdersGuiFactory;
import dev.the_fireplace.overlord.domain.entity.OrderableEntity;
import dev.the_fireplace.overlord.domain.network.ClientToServerPacketIDs;
import dev.the_fireplace.overlord.domain.network.client.SaveAIPacketBufferBuilder;
import dev.the_fireplace.overlord.model.aiconfig.AISettings;
import dev.the_fireplace.overlord.model.aiconfig.combat.CombatCategory;
import dev.the_fireplace.overlord.model.aiconfig.misc.MiscCategory;
import dev.the_fireplace.overlord.model.aiconfig.movement.EnumMovementMode;
import dev.the_fireplace.overlord.model.aiconfig.movement.MovementCategory;
import dev.the_fireplace.overlord.model.aiconfig.movement.PositionSetting;
import dev.the_fireplace.overlord.model.aiconfig.tasks.TasksCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Implementation
public final class SkeletonOrdersGuiFactory implements OrdersGuiFactory {
	private static final String TRANSLATION_BASE = "gui." + Overlord.MODID + ".aisettings.";
	private static final String OPTION_TRANSLATION_BASE = TRANSLATION_BASE + "option.";
	private static final String COMBAT_TRANSLATION_BASE = OPTION_TRANSLATION_BASE + "combat.";
	private static final String MOVEMENT_TRANSLATION_BASE = OPTION_TRANSLATION_BASE + "movement.";
	private static final String TASK_TRANSLATION_BASE = OPTION_TRANSLATION_BASE + "task.";
	private static final String MISC_TRANSLATION_BASE = OPTION_TRANSLATION_BASE + "misc.";

	private final AISettings defaultSettings = new AISettings();
	private final Translator translator;
	private final ConfigScreenBuilderFactory configScreenBuilderFactory;
	private final ClientToServerPacketIDs clientToServerPacketIDs;
	private final SaveAIPacketBufferBuilder saveAIPacketBufferBuilder;
	private ConfigScreenBuilder screenBuilder;

	@Inject
	public SkeletonOrdersGuiFactory(
		TranslatorFactory translatorFactory,
		ConfigScreenBuilderFactory configScreenBuilderFactory,
		ClientToServerPacketIDs clientToServerPacketIDs,
		SaveAIPacketBufferBuilder saveAIPacketBufferBuilder
	) {
		this.translator = translatorFactory.getTranslator(Overlord.MODID);
		this.configScreenBuilderFactory = configScreenBuilderFactory;
		this.clientToServerPacketIDs = clientToServerPacketIDs;
		this.saveAIPacketBufferBuilder = saveAIPacketBufferBuilder;
	}

	@Override
	public Screen build(Screen parent, OrderableEntity aiEntity) {
		this.screenBuilder = configScreenBuilderFactory.create(
			translator,
			TRANSLATION_BASE + "name",
			TRANSLATION_BASE + "combat",
			parent,
			() -> ClientPlayNetworking.send(
				clientToServerPacketIDs.saveAiPacketID(),
				saveAIPacketBufferBuilder.build(aiEntity)
			)
		);

		buildCategories(aiEntity.getAISettings());

		return this.screenBuilder.build();
	}

	private void buildCategories(AISettings currentSettings) {
		addCombatCategory(currentSettings.getCombat());

		this.screenBuilder.startCategory(TRANSLATION_BASE + "movement");
		addMovementCategory(currentSettings.getMovement());

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			this.screenBuilder.startCategory(TRANSLATION_BASE + "tasks");
			addTasksCategory(currentSettings.getTasks());

			this.screenBuilder.startCategory(TRANSLATION_BASE + "misc");
			addMiscCategory(currentSettings.getMisc());
		}
	}

	private void addCombatCategory(CombatCategory currentSettings) {
		CombatCategory defaults = defaultSettings.getCombat();
		OptionBuilder<Boolean> enabled = this.screenBuilder.addBoolToggle(
			OPTION_TRANSLATION_BASE + "enabled",
			currentSettings.isEnabled(),
			defaults.isEnabled(),
			currentSettings::setEnabled
		).setDescriptionRowCount((byte) 0);
		this.screenBuilder.addBoolToggle(
			COMBAT_TRANSLATION_BASE + "onlyDefend",
			currentSettings.isOnlyDefendPlayer(),
			defaults.isOnlyDefendPlayer(),
			currentSettings::setOnlyDefendPlayer
		).addDependency(enabled);
		addMeleeSettings(currentSettings, defaults, enabled);
		addRangedSettings(currentSettings, defaults, enabled);
	}

	private void addMeleeSettings(CombatCategory currentSettings, CombatCategory defaults, OptionBuilder<Boolean> enabled) {
		OptionBuilder<Boolean> meleeEnabled = this.screenBuilder.addBoolToggle(
			COMBAT_TRANSLATION_BASE + "melee",
			currentSettings.isMelee(),
			defaults.isMelee(),
			currentSettings::setMelee
		).setDescriptionRowCount((byte) 0).addDependency(enabled);
		this.screenBuilder.startSubCategory(TRANSLATION_BASE + "combat.meleeSwitching");
		this.screenBuilder.addBoolToggle(
			COMBAT_TRANSLATION_BASE + "switchToMeleeWhenNoAmmo",
			currentSettings.isSwitchToMeleeWhenNoAmmo(),
			defaults.isSwitchToMeleeWhenNoAmmo(),
			currentSettings::setSwitchToMeleeWhenNoAmmo
		).setDescriptionRowCount((byte) 0).addDependency(meleeEnabled);
		OptionBuilder<Boolean> switchToMeleeWhenClose = this.screenBuilder.addBoolToggle(
			COMBAT_TRANSLATION_BASE + "switchToMeleeWhenClose",
			currentSettings.isSwitchToMeleeWhenClose(),
			defaults.isSwitchToMeleeWhenClose(),
			currentSettings::setSwitchToMeleeWhenClose
		).setDescriptionRowCount((byte) 0).addDependency(meleeEnabled);
		this.screenBuilder.addByteSlider(
			COMBAT_TRANSLATION_BASE + "switchToMeleeDistance",
			currentSettings.getMeleeSwitchDistance(),
			defaults.getMeleeSwitchDistance(),
			currentSettings::setMeleeSwitchDistance,
			(byte) 1,
			Byte.MAX_VALUE
		).setDescriptionRowCount((byte) 0).addDependency(switchToMeleeWhenClose);
		this.screenBuilder.endSubCategory();
	}

	private void addRangedSettings(CombatCategory currentSettings, CombatCategory defaults, OptionBuilder<Boolean> enabled) {
		OptionBuilder<Boolean> rangedEnabled = this.screenBuilder.addBoolToggle(
			COMBAT_TRANSLATION_BASE + "ranged",
			currentSettings.isRanged(),
			defaults.isRanged(),
			currentSettings::setRanged
		).setDescriptionRowCount((byte) 0).addDependency(enabled);
		this.screenBuilder.startSubCategory(TRANSLATION_BASE + "combat.rangedSwitching");
		OptionBuilder<Boolean> switchToRangedWhenFar = this.screenBuilder.addBoolToggle(
			COMBAT_TRANSLATION_BASE + "switchToRangedWhenFar",
			currentSettings.isSwitchToRangedWhenFar(),
			defaults.isSwitchToRangedWhenFar(),
			currentSettings::setSwitchToRangedWhenFar
		).setDescriptionRowCount((byte) 0).addDependency(rangedEnabled);
		this.screenBuilder.addByteSlider(
			COMBAT_TRANSLATION_BASE + "switchToRangedDistance",
			currentSettings.getRangedSwitchDistance(),
			defaults.getRangedSwitchDistance(),
			currentSettings::setRangedSwitchDistance,
			(byte) 2,
			Byte.MAX_VALUE
		).setDescriptionRowCount((byte) 0).addDependency(switchToRangedWhenFar);
		this.screenBuilder.endSubCategory();
	}

	private void addMovementCategory(MovementCategory currentSettings) {
		MovementCategory defaults = defaultSettings.getMovement();
		OptionBuilder<Boolean> enabled = this.screenBuilder.addBoolToggle(
			OPTION_TRANSLATION_BASE + "enabled",
			currentSettings.isEnabled(),
			defaults.isEnabled(),
			currentSettings::setEnabled
		).setDescriptionRowCount((byte) 0);
		OptionBuilder<EnumMovementMode> movementMode = this.screenBuilder.addEnumDropdown(
			MOVEMENT_TRANSLATION_BASE + "moveMode",
			currentSettings.getMoveMode(),
			defaults.getMoveMode(),
			EnumMovementMode.values(),
			currentSettings::setMoveMode
		).addDependency(enabled).setDescriptionRowCount((byte) 0);
		this.screenBuilder.addByteSlider(
				MOVEMENT_TRANSLATION_BASE + "minimumFollowDistance",
				currentSettings.getMinimumFollowDistance(),
				defaults.getMinimumFollowDistance(),
				currentSettings::setMinimumFollowDistance,
				(byte) 1,
				Byte.MAX_VALUE
			)
			.addDependency(movementMode, mode -> mode == EnumMovementMode.FOLLOW)
			.setDescriptionRowCount((byte) 0);
		this.screenBuilder.addByteSlider(
				MOVEMENT_TRANSLATION_BASE + "maximumFollowDistance",
				currentSettings.getMaximumFollowDistance(),
				defaults.getMaximumFollowDistance(),
				currentSettings::setMaximumFollowDistance,
				(byte) 1,
				Byte.MAX_VALUE
			)
			.addDependency(movementMode, mode -> mode == EnumMovementMode.FOLLOW)
			.setDescriptionRowCount((byte) 0);
		OptionBuilder<Boolean> isExploringWander = this.screenBuilder.addBoolToggle(
			MOVEMENT_TRANSLATION_BASE + "exploringWander",
			currentSettings.isExploringWander(),
			defaults.isExploringWander(),
			currentSettings::setExploringWander
		).addDependency(movementMode, mode -> mode == EnumMovementMode.WANDER);
		this.screenBuilder.addByteSlider(
				MOVEMENT_TRANSLATION_BASE + "wanderRadius",
				currentSettings.getMoveRadius(),
				defaults.getMoveRadius(),
				currentSettings::setMoveRadius,
				(byte) 2,
				Byte.MAX_VALUE
			)
			.addDependency(movementMode, mode -> mode == EnumMovementMode.WANDER)
			.addDependency(isExploringWander, explore -> !explore);
		this.screenBuilder.addBoolToggle(
				MOVEMENT_TRANSLATION_BASE + "returnHome",
				currentSettings.isStationedReturnHome(),
				defaults.isStationedReturnHome(),
				currentSettings::setStationedReturnHome
			)
			.addDependency(movementMode, mode -> mode == EnumMovementMode.STATIONED);
		this.addPositionSetting(
			MOVEMENT_TRANSLATION_BASE + "home",
			currentSettings.getHome(),
			defaults.getHome(),
			currentSettings::setHome
		).addDependency(movementMode, mode -> mode == EnumMovementMode.STATIONED || mode == EnumMovementMode.WANDER);
	}

	private void addTasksCategory(TasksCategory currentSettings) {
		TasksCategory defaults = defaultSettings.getTasks();
		this.screenBuilder.addBoolToggle(
			OPTION_TRANSLATION_BASE + "enabled",
			currentSettings.isEnabled(),
			defaults.isEnabled(),
			currentSettings::setEnabled
		).setDescriptionRowCount((byte) 0);
		OptionBuilder<Boolean> woodcutting = this.screenBuilder.addBoolToggle(
			TASK_TRANSLATION_BASE + "woodcutting",
			currentSettings.isWoodcutting(),
			defaults.isWoodcutting(),
			currentSettings::setWoodcutting
		).setDescriptionRowCount((byte) 0);
		this.addUniversalList(
			TASK_TRANSLATION_BASE + "woodcuttingBlockList",
			currentSettings.getWoodcuttingBlockList(),
			defaults.getWoodcuttingBlockList(),
			currentSettings::setWoodcuttingBlockList
		).addDependency(woodcutting);
		this.screenBuilder.addBoolToggle(
			TASK_TRANSLATION_BASE + "woodcuttingWithoutTools",
			currentSettings.isWoodcuttingWithoutTools(),
			defaults.isWoodcuttingWithoutTools(),
			currentSettings::setWoodcuttingWithoutTools
		).setDescriptionRowCount((byte) 0).addDependency(woodcutting);
	}

	private void addMiscCategory(MiscCategory currentSettings) {
		MiscCategory defaults = defaultSettings.getMisc();
		OptionBuilder<Boolean> saveDamagedEquipment = this.screenBuilder.addBoolToggle(
			MISC_TRANSLATION_BASE + "saveDamagedEquipment",
			currentSettings.isSaveDamagedEquipment(),
			defaults.isSaveDamagedEquipment(),
			currentSettings::setSaveDamagedEquipment
		);
		addUniversalList(
			MISC_TRANSLATION_BASE + "saveEquipmentList",
			currentSettings.getSaveEquipmentList(),
			defaults.getSaveEquipmentList(),
			currentSettings::setSaveEquipmentList
		).addDependency(saveDamagedEquipment);
		this.screenBuilder.addBoolToggle(
			MISC_TRANSLATION_BASE + "loadChunks",
			currentSettings.isLoadChunks(),
			defaults.isLoadChunks(),
			currentSettings::setLoadChunks
		);
	}

	private CustomButtonBuilder<String> addUniversalList(
		String optionTranslationBase,
		UUID currentValue,
		UUID defaultValue,
		Consumer<UUID> saveFunction
	) {
		return this.screenBuilder.addCustomOptionButton(
			optionTranslationBase,
			currentValue.toString(),
			defaultValue.toString(),
			stringValue -> saveFunction.accept(UUID.fromString(stringValue)),
			(parent, current) -> new ListBuilderGui(translator.getTranslatedText(optionTranslationBase), parent, current)
		);
	}

	private CustomButtonBuilder<String> addPositionSetting(
		String optionTranslationBase,
		PositionSetting currentValue,
		PositionSetting defaultValue,
		Consumer<PositionSetting> saveFunction
	) {
		return this.screenBuilder.addCustomOptionButton(
			optionTranslationBase,
			currentValue.toString(),
			defaultValue.toString(),
			stringValue -> saveFunction.accept(PositionSetting.fromString(stringValue)),
			(parent, current) -> new PositionSelectorGui(translator.getTranslatedText(optionTranslationBase), parent, current)
		);
	}
}
