/*
 * FitFunctionsOnRatioDataView.java
 *
 * Created Jul 6, 2011
 *
 * Copyright 2006-2015 James F. Bowring and www.Earth-Time.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.earthtime.Tripoli.dataViews.simpleViews.usedByReflection;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JLayeredPane;
import org.earthtime.Tripoli.dataModels.DataModelFitFunctionInterface;
import org.earthtime.Tripoli.dataModels.DataModelInterface;
import org.earthtime.Tripoli.dataModels.MaskingSingleton;
import org.earthtime.Tripoli.dataModels.RawRatioDataModel;
import org.earthtime.Tripoli.dataViews.AbstractRawDataView;
import org.earthtime.Tripoli.dataViews.simpleViews.FitFunctionDataInterface;
import org.earthtime.Tripoli.fractions.TripoliFraction;
import org.earthtime.dataDictionaries.DataPresentationModeEnum;
import org.earthtime.dataDictionaries.FitFunctionTypeEnum;
import org.earthtime.utilities.TicGeneratorForAxes;

/**
 *
 * @author James F. Bowring
 */
public class FitFunctionsOnRatioDataView extends AbstractRawDataView implements FitFunctionDataInterface {

    /**
     *
     */
    public static int DEFAULT_WIDTH_OF_PANE = 128;
    //
    //private DataModelInterface rawRatioDataModel;
    private double[] fittedFunctionValues;
    private boolean showFittedFunction;

    /**
     *
     * @param sampleSessionDataView
     * @param tripoliFraction
     * @param rawRatioDataModel
     * @param bounds
     * @param invokeMouseListener
     */
    public FitFunctionsOnRatioDataView(//
            JLayeredPane sampleSessionDataView, //
            TripoliFraction tripoliFraction,//
            DataModelInterface rawRatioDataModel,//
            Rectangle bounds,//
            boolean invokeMouseListener) {
        super(sampleSessionDataView, tripoliFraction, bounds, invokeMouseListener, true);

        this.rawRatioDataModel = rawRatioDataModel;
        this.fittedFunctionValues = null;
        this.showFittedFunction = false;

        this.standardValue = rawRatioDataModel.getStandardValue();
    }

    /**
     *
     * @param g2d
     */
    @Override
    public void paint(Graphics2D g2d) {
        super.paint(g2d);

        if (myOnPeakData != null) {

            // nov 2014
            if ((((DataModelFitFunctionInterface) rawRatioDataModel).getSelectedFitFunctionType().compareTo(FitFunctionTypeEnum.MEANRATIO) == 0)//
                    && //
                    (dataPresentationMode.compareTo(DataPresentationModeEnum.RATIO) != 0)) {
                g2d.drawString(dataPresentationMode.getName() + " NOT DEFINED", 25, 25);
            } else if ((rawRatioDataModel.isUsedForCommonLeadCorrections())//
                    && //
                    (dataPresentationMode.compareTo(DataPresentationModeEnum.ALPHA) == 0)) {
                g2d.drawString(dataPresentationMode.getName() + " NOT DEFINED", 25, 25);
            } else {

                // feb 2013 change of approach: instead of red lines, just bypass the point altogether
                // find first active point
                boolean[] dataActiveMap = rawRatioDataModel.getDataActiveMap();//nov 2014 changed t ratio to show differences        tripoliFraction.getDataActiveMap();
                int firstActiveIndex = 0;
                for (int i = 0; i < dataActiveMap.length; i++) {
                    if (dataActiveMap[i]) {
                        break;
                    } else {
                        firstActiveIndex++;
                    }
                }

                // draw connecting line if possible
                if (firstActiveIndex < dataActiveMap.length) {
                    Shape connectingLine = new Path2D.Double();
                    g2d.setPaint(tripoliFraction.isIncluded() ? getPaintColor() : EXCLUDED_COLOR);
                    g2d.setStroke(new BasicStroke(0.75f));
                    ((Path2D) connectingLine).moveTo(//
                            mapX(myOnPeakNormalizedAquireTimes[firstActiveIndex]), //
                            mapY(myOnPeakData[firstActiveIndex]));

                    for (int i = firstActiveIndex + 1; i < myOnPeakData.length; i++) {
                        if (dataActiveMap[i]) {
                            ((Path2D) connectingLine).lineTo( //
                                    mapX(myOnPeakNormalizedAquireTimes[i]), mapY(myOnPeakData[i]));
                        }
                    }
                    g2d.draw(connectingLine);

                }

                // data points
                g2d.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < myOnPeakData.length; i++) {
                    // nov 2014 to handle */Pb204
                    Shape dataPoint = null;
                    // logs cant be nan and ratios or alphas cant be neg
                    if (!rawRatioDataModel.isForceMeanForCommonLeadRatios() //
                            && //
                            Double.isNaN(myOnPeakData[i])){
                        Font specialFont = new Font("Courier New", Font.PLAIN, 10);
                        GlyphVector vect = specialFont.createGlyphVector(g2d.getFontRenderContext(), "+");
                        dataPoint = vect.getOutline((float) mapX(myOnPeakNormalizedAquireTimes[i]) - 3, (float) mapY(minY));
                    } else {
                        dataPoint = new java.awt.geom.Ellipse2D.Double( //
                                mapX(myOnPeakNormalizedAquireTimes[i]), mapY(myOnPeakData[i]), 1, 1);
                    }
                    g2d.setPaint(determineDataColor(i, getPaintColor()));

                    g2d.draw(dataPoint);
                }

                if (showFittedFunction) {
                    // draw fittedFunctionAverageLine for intercept case
                    Shape fittedFunctionAverageLine = new Path2D.Double();
                    g2d.setPaint(Color.RED);
                    g2d.setStroke(new BasicStroke(3.0f));
                    ((Path2D) fittedFunctionAverageLine).moveTo(//
                            mapX(myOnPeakNormalizedAquireTimes[0]), //
                            mapY(fittedFunctionValues[0]));

                    for (int i = 1; i < myOnPeakData.length; i++) {
                        ((Path2D) fittedFunctionAverageLine).lineTo( //
                                mapX(myOnPeakNormalizedAquireTimes[i]), mapY(fittedFunctionValues[i]));
                    }
                    g2d.draw(fittedFunctionAverageLine);
                }

                // draw masking shades
                boolean[] maskingArray = MaskingSingleton.getInstance().getMaskingArray();
                //left
                int leftEdgeIndex = -1;
                boolean leftEdgeFound = false;
                int rightEdgeIndex = maskingArray.length;
                for (int i = 0; i < maskingArray.length; i++) {
                    if ((!leftEdgeFound) && (!maskingArray[i])) {
                        leftEdgeIndex = i;
                    } else {
                        leftEdgeFound = true;
                    }

                    if (leftEdgeFound && !maskingArray[i]) {
                        rightEdgeIndex = i;
                        break;
                    }
                }

                Composite originalComposite = g2d.getComposite();
                g2d.setPaint(Color.gray);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));

                if (leftEdgeIndex > -1) {
                    Shape leftShade = new Rectangle2D.Double( //
                            mapX(minX), //
                            -1,
                            mapX((double) myOnPeakNormalizedAquireTimes[leftEdgeIndex] + (double) (myOnPeakNormalizedAquireTimes[1] - myOnPeakNormalizedAquireTimes[0]) / 2.0),//
                            getHeight() + 1);

                    g2d.fill(leftShade);
                }

                if (rightEdgeIndex < maskingArray.length) {
                    Shape rightShade = new Rectangle2D.Double( //
                            mapX((double) myOnPeakNormalizedAquireTimes[rightEdgeIndex] - (double) (myOnPeakNormalizedAquireTimes[1] - myOnPeakNormalizedAquireTimes[0]) / 2.0),//
                            -1,
                            mapX(maxX), //
                            getHeight() + 1);

                    g2d.fill(rightShade);
                }

                g2d.setComposite(originalComposite);
            }
        } else {
            g2d.drawString("BELOW DETECTION", 25, 25);
        }
    }

    /**
     *
     */
    @Override
    public void updateFittedData() {

        if (!notShownDueToBelowDetectionFlag) {

            fittedFunctionValues = ((RawRatioDataModel) rawRatioDataModel).getFitFunctionLogValues().clone();

            // X-axis lays out time evenly spaced
            minX = myOnPeakNormalizedAquireTimes[0];
            maxX = myOnPeakNormalizedAquireTimes[myOnPeakNormalizedAquireTimes.length - 1];

            // Y-axis is ratios
            minY = Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;

            // choose data and walk data and get min and max for axes
            // nov 2014
            if (((DataModelFitFunctionInterface) rawRatioDataModel).getSelectedFitFunctionType().compareTo(FitFunctionTypeEnum.MEANRATIO) == 0) {
                myOnPeakData = ((RawRatioDataModel) rawRatioDataModel).getRatios().clone();
            } else {
                myOnPeakData = ((RawRatioDataModel) rawRatioDataModel).getLogRatios().clone();
                for (int i = 0; i < myOnPeakData.length; i++) {
                    double convertedOnPeak = convertLogDatumToPresentationMode(myOnPeakData[i]);//convertLogDatumToPresentationMode(myOnPeakData[i], myRatiosOnPeakData[i]);// 
                    myOnPeakData[i] = convertedOnPeak;
                    double convertedFitFunc = convertLogDatumToPresentationMode(fittedFunctionValues[i]);
                    fittedFunctionValues[i] = convertedFitFunc;
                }
            }

            // find min and max y
            for (int i = 0; i < myOnPeakData.length; i++) {

                if (!Double.isNaN(myOnPeakData[i])) {
                    minY = Math.min(minY, myOnPeakData[i]);
                    maxY = Math.max(maxY, myOnPeakData[i]);
                }
                minY = Math.min(minY, fittedFunctionValues[i]);
                maxY = Math.max(maxY, fittedFunctionValues[i]);
            }

            // adjust margins for unknowns
            if (!tripoliFraction.isStandard()) {
                double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.05);
                minX -= xMarginStretch;
                maxX += xMarginStretch;

                double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 12.0 / this.getHeight());//    0.05 );
                minY -= yMarginStretch;
                maxY += yMarginStretch;
            }
        }
    }

    /**
     *
     */
    @Override
    public void preparePanel() {

        this.removeAll();

        setDisplayOffsetY(0.0);
        setDisplayOffsetX(0.0);

        // normalize aquireTimes
        myOnPeakNormalizedAquireTimes = rawRatioDataModel.getNormalizedOnPeakAquireTimes();

//        // X-axis lays out time evenly spaced
//        minX = myOnPeakNormalizedAquireTimes[0];
//        maxX = myOnPeakNormalizedAquireTimes[myOnPeakNormalizedAquireTimes.length - 1];
        notShownDueToBelowDetectionFlag = rawRatioDataModel.isBelowDetection();

        updateFittedData();

    }

    /**
     *
     * @return
     */
    @Override
    public DataModelInterface getDataModel() {
        return rawRatioDataModel;
    }

    /**
     * @param showFittedFunction the showFittedFunction to set
     */
    public void setShowFittedFunction(boolean showFittedFunction) {
        this.showFittedFunction = showFittedFunction;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean amShowingUnknownFraction() {
        return !tripoliFraction.isStandard();
    }
}
