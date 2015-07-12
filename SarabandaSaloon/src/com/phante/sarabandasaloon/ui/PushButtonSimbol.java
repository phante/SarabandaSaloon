/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.ui;

import com.phante.sarabandasaloon.entity.PushButtonStatus;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author deltedes
 */
public class PushButtonSimbol extends StackPane {
    public static final String ENABLED = "M134.238,0.001C60.099,0.001,0,60.098,0,134.238l0.001,62.645c0,29.831,26.848,26.848,26.848,26.848v-89.492c0-16.038,6.711-31.171,13.164-44.747l4.734,0.001c13.284,0,19.984-7.501,19.984-7.501l0.072,0.073c15.863-21.122,40.989-37.318,69.436-37.318c28.447,0,53.573,16.196,69.435,37.318l0.074-0.073c0,0,6.7,7.501,19.983,7.501l4.734-0.001c6.453,13.576,13.165,28.709,13.165,44.747v89.492c0,0,26.848,2.517,26.848-26.848v-62.645C268.477,60.098,208.378,0.001,134.238,0.001z M67.12,134.238c-17.299,0-31.322,14.023-31.322,31.322v71.594c0,17.299,14.023,31.322,31.322,31.322c17.299,0,31.322-14.024,31.322-31.322v-71.594C98.442,148.262,84.419,134.238,67.12,134.238z M201.357,134.238c-17.298,0-31.322,14.023-31.322,31.322v71.594c0,17.299,14.024,31.322,31.322,31.322c17.299,0,31.323-14.024,31.323-31.322v-71.594C232.68,148.262,218.657,134.238,201.357,134.238z";
    public static final String PRESSED = "M232.943,223.73H35.533c-12.21,0-22.109,10.017-22.109,22.373c0,12.356,9.9,22.373,22.109,22.373h197.41c12.21,0,22.109-10.017,22.109-22.373C255.052,233.747,245.153,223.73,232.943,223.73zM117.881,199.136c4.034,4.041,9.215,6.147,14.491,6.508c0.626,0.053,1.227,0.188,1.866,0.188c0.633,0,1.228-0.135,1.847-0.186c5.284-0.357,10.473-2.464,14.512-6.51l70.763-70.967c8.861-8.875,8.861-23.267,0-32.142c-8.86-8.876-23.225-8.876-32.086,0l-32.662,32.756V22.373C156.612,10.017,146.596,0,134.238,0c-12.356,0-22.372,10.017-22.372,22.373v106.41L79.204,96.027c-8.86-8.876-23.226-8.876-32.086,0c-8.86,8.875-8.86,23.267,0,32.142L117.881,199.136z";
    public static final String ERROR = "M131.804,106.491l75.936-75.936c6.99-6.99,6.99-18.323,0-25.312c-6.99-6.99-18.322-6.99-25.312,0l-75.937,75.937L30.554,5.242c-6.99-6.99-18.322-6.99-25.312,0c-6.989,6.99-6.989,18.323,0,25.312l75.937,75.936L5.242,182.427c-6.989,6.99-6.989,18.323,0,25.312c6.99,6.99,18.322,6.99,25.312,0l75.937-75.937l75.937,75.937c6.989,6.99,18.322,6.99,25.312,0c6.99-6.99,6.99-18.322,0-25.312L131.804,106.491z";
    public static final String DISABLED = "M245.102,143.151l36.98-37.071c5.593-5.605,5.593-14.681,0-20.284l-10.124-10.142c-5.593-5.604-14.655-5.604-20.247,0l-36.98,37.071l-36.977-37.043c-5.594-5.603-14.654-5.603-20.247,0l-10.124,10.143c-5.594,5.603-5.594,14.679,0,20.282l36.987,37.053l-36.961,37.051c-5.591,5.604-5.591,14.681,0,20.284l10.126,10.141c5.593,5.604,14.654,5.604,20.247,0l36.96-37.05l36.97,37.035c5.592,5.605,14.654,5.605,20.247,0l10.124-10.141c5.593-5.603,5.593-14.68,0-20.282L245.102,143.151z M108.674,48.296L44.747,98.42H17.9c-13.228,0-17.899,4.826-17.899,17.898L0,142.719l0.001,27.295c0,13.072,4.951,17.898,17.899,17.898h26.847l63.927,50.068c7.667,4.948,16.557,6.505,16.557-7.365V55.662C125.23,41.792,116.341,43.349,108.674,48.296z";
    
    public Map<PushButtonStatus, SVGPath> simbols = new HashMap<>();
            
    public PushButtonSimbol () {
        super();
        
        for (PushButtonStatus status: PushButtonStatus.values()) {
            SVGPath simbol = new SVGPath();
            simbol.setFillRule(FillRule.EVEN_ODD);
            simbol.setVisible(status == PushButtonStatus.ENABLED);
            simbol.setContent(ERROR);
            
            getChildren().add(simbol);
            simbols.put(status, simbol);
            
            heightProperty().addListener(listener -> {
                simbol.scaleXProperty().setValue(getHeight()/simbol.maxHeight(Double.MAX_VALUE));
            });
            
            widthProperty().addListener(listener -> {
                simbol.scaleYProperty().setValue(getWidth()/simbol.maxWidth(Double.MAX_VALUE));
            });
        }
        
        simbols.get(PushButtonStatus.ENABLED).setContent(ENABLED);
        simbols.get(PushButtonStatus.ENABLED).setFill(Color.GOLD);
        simbols.get(PushButtonStatus.PRESSED).setContent(PRESSED);
        simbols.get(PushButtonStatus.PRESSED).setFill(Color.GREEN);
        simbols.get(PushButtonStatus.ERROR).setContent(ERROR);
        simbols.get(PushButtonStatus.ERROR).setFill(Color.RED);
        simbols.get(PushButtonStatus.DISABLED).setContent(DISABLED);
        simbols.get(PushButtonStatus.DISABLED).setFill(Color.GREY);
    }
    
    public void setValue(PushButtonStatus newStatus) {
        for (PushButtonStatus status: PushButtonStatus.values()) {
            //System.out.println("ButtonSimbol.setValue(" + newStatus + ") " + status + "->" + (status == newStatus));
            simbols.get(status).setVisible(status == newStatus);
        }
    }
}
