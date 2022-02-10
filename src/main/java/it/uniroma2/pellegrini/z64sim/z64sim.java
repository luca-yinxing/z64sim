/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import it.uniroma2.pellegrini.z64sim.controller.MainController;
import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;
import it.uniroma2.pellegrini.z64sim.view.Splash;

public class z64sim {

    public static void main(String[] args) {
        Splash splashScreen = new Splash(4);

        splashScreen.step("Loading settings");
        SettingsController.init();
        splashScreen.step("Initializing logger");
        Logger.init(); // *Must* come after settings initialization

        splashScreen.step("Initializing controllers");
        MainController.init();

        splashScreen.step("Initializing UI");
        if(SettingsController.getTheme().equals("light"))
            FlatLightLaf.setup();
        else
            FlatDarkLaf.setup();

        MainWindow mainWindow = MainWindow.getInstance();

        splashScreen.close();
        mainWindow.show();
    }
}