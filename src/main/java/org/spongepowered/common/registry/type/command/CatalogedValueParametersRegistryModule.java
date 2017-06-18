/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.registry.type.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.parameters.specification.CatalogedValueParameter;
import org.spongepowered.api.command.parameters.specification.CatalogedValueParameters;
import org.spongepowered.api.command.parameters.specification.ValueParameters;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.common.command.parameters.valueparameters.CatalogableCatalogTypeValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.CatalogableChoicesValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.CatalogableNumberValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.DurationValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.EntityValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.JoinedStringValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.LocationValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.NoneValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.PlayerValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.PluginContainerValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.RawJoinedStringValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.StringValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.UserValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.Vector3dValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.WorldPropertiesValueParameter;

import java.util.*;

public class CatalogedValueParametersRegistryModule implements AdditionalCatalogRegistryModule<CatalogedValueParameter> {

    @RegisterCatalog(CatalogedValueParameters.class)
    private final Map<String, CatalogedValueParameter> parserModifierMappings = Maps.newHashMap();
    private final Map<String, CatalogedValueParameter> idMappings = Maps.newHashMap();

    @Override
    public void registerAdditionalCatalog(CatalogedValueParameter extraCatalog) {
        Preconditions.checkArgument(!idMappings.containsKey(extraCatalog.getId().toLowerCase(Locale.ENGLISH)), "That ID has already been "
                + "registered.");

        this.idMappings.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public Optional<CatalogedValueParameter> getById(String id) {
        return Optional.ofNullable(this.idMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<CatalogedValueParameter> getAll() {
        return ImmutableSet.copyOf(this.idMappings.values());
    }

    @Override
    public void registerDefaults() {
        final Map<String, Boolean> booleanChoices = new HashMap<String, Boolean>() {{
            put("true", true);
            put("t", true);
            put("yes", true);
            put("y", true);
            put("verymuchso", true);

            put("false", false);
            put("f", false);
            put("no", false);
            put("n", false);
            put("notatall", false);
        }};
        this.parserModifierMappings.put("boolean", new CatalogableChoicesValueParameter("sponge:boolean", "Boolean", () -> booleanChoices,
                Tristate.TRUE));

        CatalogableCatalogTypeValueParameter<DimensionType> ccdt = new CatalogableCatalogTypeValueParameter<>(
                "sponge:dimension_catalog_type",
                "Dimension Catalog Type",
                DimensionType.class,
                "minecraft",
                "sponge"
        );
        this.parserModifierMappings.put("dimension", ccdt);

        this.parserModifierMappings.put("duration", new DurationValueParameter());

        this.parserModifierMappings.put("double",
                new CatalogableNumberValueParameter("sponge:double", "Double parameter", "a double", Double::parseDouble));
        this.parserModifierMappings.put("integer",
                new CatalogableNumberValueParameter("sponge:integer", "Integer parameter", "an integer", Integer::parseInt));
        this.parserModifierMappings.put("long",
                new CatalogableNumberValueParameter("sponge:long", "Long parameter", "a long integer", Integer::parseInt));

        this.parserModifierMappings.put("none", new NoneValueParameter());

        this.parserModifierMappings.put("entity", new EntityValueParameter("sponge:entity", "Entity parameter", false));
        this.parserModifierMappings.put("entity_or_source",
                new EntityValueParameter("sponge:entity_or_source", "Entity or source parameter", true));

        PlayerValueParameter parameter = new PlayerValueParameter("sponge:player", "Player parameter", false);
        this.parserModifierMappings.put("player", parameter);
        this.parserModifierMappings.put("player_or_source",
                new PlayerValueParameter("sponge:player_or_source", "Player or source parameter", true));

        this.parserModifierMappings.put("plugin", new PluginContainerValueParameter());

        this.parserModifierMappings.put("remaining_joined_strings", new JoinedStringValueParameter());
        this.parserModifierMappings.put("remaining_raw_joined_strings", new RawJoinedStringValueParameter());

        this.parserModifierMappings.put("string", new StringValueParameter());

        this.parserModifierMappings.put("user", new UserValueParameter("sponge:user", "User parameter", parameter, false));
        this.parserModifierMappings.put("user_or_source", new UserValueParameter("sponge:user_or_source", "User or source parameter", parameter,
                false));

        Vector3dValueParameter vector3dValueParameter = new Vector3dValueParameter();
        this.parserModifierMappings.put("vector3d", vector3dValueParameter);

        WorldPropertiesValueParameter worldPropertiesValueParameter = new WorldPropertiesValueParameter(ccdt);
        this.parserModifierMappings.put("world_properties", worldPropertiesValueParameter);

        this.parserModifierMappings.put("location", new LocationValueParameter(worldPropertiesValueParameter, vector3dValueParameter));

        this.parserModifierMappings.forEach((k, v) -> this.idMappings.put(v.getId().toLowerCase(Locale.ENGLISH), v));
    }

}
