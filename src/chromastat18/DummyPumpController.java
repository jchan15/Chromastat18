/*
 * Copyright (C) 2018 WilliamKwok
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package chromastat18;

import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DummyPumpController controls 3 faux syringe pumps
 * @author WilliamKwok
 */
public class DummyPumpController extends Thread {
    private ArrayList<DummyPump> pumps = new ArrayList<>();

    private int pumpMoving = -1;
    private boolean calibrated = false;

//    // Uncomment this for debug methods
//    private ArrayList<SyringePump> pumps2 = new ArrayList<>();
//    private final MCP mcpProviderOne;
//    private final MCP mcpProviderTwo; 
    
    
    
    public DummyPumpController() throws I2CFactory.UnsupportedBusNumberException, IOException {
        for(int i = 0; i < 3; i++) {
            pumps.add(new DummyPump());
        }
        // Uncomment below for debug methods for an actual pump
//        mcpProviderOne = new MCP(0x20);
//        mcpProviderTwo = new MCP(0x21);
//        Map<String, Pin> inarg1 = new HashMap<>();
//        Map<String, Pin> inarg2 = new HashMap<>();
//        Map<String, Pin> inarg3 = new HashMap<>();
        
//        String[] keys = {"dirPin", "stepPin", "enablePin", "minPin", "maxPin"};
//        Pin[] pins1 = {MCP23017Pin.GPIO_A2, MCP23017Pin.GPIO_A1, MCP23017Pin.GPIO_A0, MCP23017Pin.GPIO_A6, MCP23017Pin.GPIO_A2};
//        Pin[] pins2 = {MCP23017Pin.GPIO_A5, MCP23017Pin.GPIO_A4, MCP23017Pin.GPIO_A3, MCP23017Pin.GPIO_A5, MCP23017Pin.GPIO_A3};
//        Pin[] pins3 = {MCP23017Pin.GPIO_B0, MCP23017Pin.GPIO_A7, MCP23017Pin.GPIO_A6, MCP23017Pin.GPIO_A4, MCP23017Pin.GPIO_A7};
//        SyringePump pump1;
//        SyringePump pump2;
//        SyringePump pump3;
    
    
//        for(int i = 0; i < keys.length; i++) {
//            inarg1.put(keys[i], pins1[i]);
//            inarg2.put(keys[i], pins2[i]);
//            inarg3.put(keys[i], pins3[i]);
//        }
        
//        pump1 = new SyringePump(inarg1, mcpProviderOne, mcpProviderTwo);
//        pump2 = new SyringePump(inarg2, mcpProviderOne, mcpProviderTwo);
//        pump3 = new SyringePump(inarg3, mcpProviderOne, mcpProviderTwo);
//        pumps2.add(pump1);
//        pumps2.add(pump2);
//        pumps2.add(pump3);
//        System.out.println("got here");
    }
    
// testpump is for testing an actual pump. This is for debug.
//    public void testpump() throws InterruptedException {
//        System.out.println("test");
//        pumps2.get(0).calibrate();
//        pumps2.get(0).getState();
//    }
    
    public int pumpMoving() {
        return this.pumpMoving;
    }
    
    public double getPumpPos(int pumpNumber) {
       return this.pumps.get(pumpNumber).position();
    }
    
    public void setNewGoal(int pumpNumber, int newGoal) {
        if(this.pumpMoving == -1) {
            pumps.get(pumpNumber).setNewGoal(newGoal);
        }
    }
    
    public boolean isCalibrated() {
        return this.calibrated;
    }
    
    public void calibrate() throws InterruptedException {
        for(int i = 0; i < 3; i++) {
            this.pumpMoving = i;
            this.pumps.get(i).calibrate();
        }
        this.pumpMoving = -1;
        this.calibrated = true;
    }
    
    public void recalibrate() {
        this.calibrated = false;
    }
    
    @Override
    public void run() {
        while(true) {
            if(this.calibrated) {
                ArrayList<Boolean> pumpsMoving = new ArrayList<>();
                for(int i = 0; i < pumps.size(); i++) {
                    pumpsMoving.add(pumps.get(i).goalMismatch());
                }
                int pumpMovingIndex = pumpsMoving.indexOf(true);
                if(pumpMovingIndex >= 0) {
                    try {
                        this.pumpMoving = pumpMovingIndex;
                        pumps.get(pumpMovingIndex).move();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DummyPumpController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    this.pumpMoving = -1;
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DummyPumpController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                try {
                    this.calibrate();
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(DummyPumpController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}
