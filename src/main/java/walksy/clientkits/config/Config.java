package walksy.clientkits.config;

import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.LocalConfig;
import main.walksy.lib.core.config.local.Category;
import main.walksy.lib.core.config.local.Option;
import main.walksy.lib.core.config.local.options.BooleanOption;
import main.walksy.lib.core.config.local.options.NumericalOption;
import main.walksy.lib.core.config.local.options.StringListOption;
import main.walksy.lib.core.config.local.options.groups.OptionGroup;
import main.walksy.lib.core.utils.PathUtils;

import java.util.List;

public class Config implements WalksyLibConfig {

    @Override
    public LocalConfig define() {
        return LocalConfig.createBuilder("Client Kits")
            .path(PathUtils.ofConfigDir("clientkits"))
            .build();
    }
}
