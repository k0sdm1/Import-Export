package com.brightpessimist.imex.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Hello World";
    public static String api_url = "http://localhost:8000/api/v1/community/get_pricelist/";

    public static void synchronizeConfiguration(File configDir) {
        Configuration configuration = new Configuration(new File(new File(configDir, "imex"), "imex.ini"));

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");
        api_url = configuration.getString(
            "api_url",
            Configuration.CATEGORY_GENERAL,
            api_url,
            "URL for API, where to get prices and available items for the Trading Bay");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
