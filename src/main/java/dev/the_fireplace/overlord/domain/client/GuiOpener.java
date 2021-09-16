package dev.the_fireplace.overlord.domain.client;

import dev.the_fireplace.overlord.domain.entity.OrderableEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface GuiOpener {
    void openOrdersGUI(OrderableEntity entity);
}
