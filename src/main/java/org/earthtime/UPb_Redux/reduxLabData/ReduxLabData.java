/*
 * ReduxLabData.java
 *
 * Created on October 9, 2006, 5:25 PM
 *
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
package org.earthtime.UPb_Redux.reduxLabData;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import org.earthtime.UPb_Redux.ReduxConstants;
import static org.earthtime.UPb_Redux.ReduxConstants.myUsersUPbReduxDataFolderName;
import org.earthtime.UPb_Redux.exceptions.BadLabDataException;
import org.earthtime.UPb_Redux.fractions.Fraction;
import org.earthtime.UPb_Redux.fractions.UPbReduxFractions.UPbFractionI;
import org.earthtime.UPb_Redux.reduxLabData.labEquipmentSettings.LabEquipmentSettings;
import org.earthtime.UPb_Redux.reports.ReportSettings;
import org.earthtime.UPb_Redux.utilities.ETSerializer;
import org.earthtime.UPb_Redux.valueModels.ValueModel;
import org.earthtime.dataDictionaries.AnalysisMeasures;
import org.earthtime.dataDictionaries.DataDictionary;
import org.earthtime.dataDictionaries.MineralTypes;
import org.earthtime.exceptions.ETException;
import org.earthtime.ratioDataModels.AbstractRatiosDataModel;
import org.earthtime.ratioDataModels.initialPbModelsET.InitialPbModelET;
import org.earthtime.ratioDataModels.mineralStandardModels.MineralStandardUPbModel;
import org.earthtime.ratioDataModels.pbBlankICModels.PbBlankICModel;
import org.earthtime.ratioDataModels.physicalConstantsModels.PhysicalConstantsModel;
import org.earthtime.ratioDataModels.rareEarthElementsModels.RareEarthElementsModel;
import org.earthtime.ratioDataModels.tracers.TracerUPbModel;

/**
 *
 * @author James F. Bowring
 */
public final class ReduxLabData implements Serializable {

    // Class variables
    private static final long serialVersionUID = -5567910355173929685L;//16L;
    private static final String labDataFileName = "ReduxLabData.ser";
    private static ReduxLabData instance = null;
    // Instance variables
    private String labName;
    private String analystName;
    private ArrayList<AbstractRatiosDataModel> tracerModels;
    private ArrayList<ValueModel> alphaUModels = new ReduxLabDataList<>("Alpha U Model");
    private ArrayList<ValueModel> alphaPbModels = new ReduxLabDataList<>("Alpha Pb Model");
    private ArrayList<AbstractRatiosDataModel> blanks;
    private ArrayList<AbstractRatiosDataModel> initialPbModels;
    private ArrayList<AbstractRatiosDataModel> physicalConstantsModels;
    private ArrayList<AbstractRatiosDataModel> mineralStandardModels;
    private ArrayList<AbstractRatiosDataModel> rareEarthElementModels;
    private ArrayList<ReportSettings> reportSettingsModels
            = new ReduxLabDataList<>("Report Settings");
    private ArrayList<LabEquipmentSettings> labEquipmentSettings = //
            new ReduxLabDataList<>(" Lab Equipment Settings");
    private AbstractRatiosDataModel defaultLabTracer;
    private ValueModel defaultLabAlphaUModel;
    private ValueModel defaultLabAlphaPbModel;
    private AbstractRatiosDataModel defaultLabPbBlank;
    private AbstractRatiosDataModel defaultLabInitialPbModel;
    private BigDecimal defaultStaceyKramersOnePctUnct;
    private BigDecimal defaultStaceyKramersCorrelationCoeffs;
    private ReportSettings defaultReportSettingsModel;
    private ValueModel defaultPbBlankMassInGrams;
    private ValueModel assumedUBlankMassInGrams;
    private ValueModel defaultR18O_16O;
    private ValueModel defaultR238_235s;
    private ValueModel defaultR238_235b;
    private ValueModel defaultTracerMass;
    private ValueModel defaultRTh_Umagma;
    private ValueModel defaultAr231_235sample;
    // additional defaults per Doug Walker suggestion Nov 2011
    private String defaultMineralName;
    private String defaultSettingType;
    private int defaultNumberOfGrains;
    private boolean defaultPhysicallyAbraded;
    private boolean defaultLeachedInHFAcid;
    private boolean defaultAnnealedAndChemicallyAbraded;
    private boolean defaultChemicallyPurifiedUPb;
    private AbstractRatiosDataModel defaultPhysicalConstantsModel;
    private AbstractRatiosDataModel defaultMineralStandardModel;
    // dec 2014
    private AbstractRatiosDataModel defaultLAICPMSPrimaryMineralStandardModel;
    private AbstractRatiosDataModel defaultRareEarthElementModel;
    private int defaultLeftShadeCountForLAICPMSAquisitions;

    /**
     * Creates a new instance of ReduxLabData
     */
    private ReduxLabData() {
        setLabName("New Lab - No Name");

        restoreBuiltInModels();

        try {
            defaultLabTracer = getFirstTracer();
        } catch (BadLabDataException ex) {
        }

        getAlphaUModels().
                add(new ValueModel(ReduxConstants.NONE, BigDecimal.ZERO, "ABS", BigDecimal.ZERO, BigDecimal.ZERO));
        getAlphaUModels().
                add(new ValueModel(
                                "example alphaU",
                                new BigDecimal("0.000876443899691577"),
                                "ABS",
                                new BigDecimal("0.0002"), BigDecimal.ZERO));

        try {
            setDefaultLabAlphaUModel(getFirstAlphaUModel());
        } catch (BadLabDataException ex) {
        }

        getAlphaPbModels().
                add(new ValueModel(ReduxConstants.NONE, BigDecimal.ZERO, "ABS", BigDecimal.ZERO, BigDecimal.ZERO));
        getAlphaPbModels().
                add(new ValueModel(
                                "example alphaPb",
                                new BigDecimal("0.0025"),
                                "ABS",
                                new BigDecimal("0.0002"), BigDecimal.ZERO));

        try {
            setDefaultLabAlphaPbModel(getFirstAlphaPbModel());
        } catch (BadLabDataException ex) {
        }

        try {
            setDefaultLabPbBlank(getFirstPbBlank());
        } catch (BadLabDataException ex) {
        }

        try {
            defaultLabInitialPbModel = getFirstInitialPbModel();
        } catch (BadLabDataException ex) {
        }

        try {
            defaultPhysicalConstantsModel = getFirstPhysicalConstantsModel();
        } catch (BadLabDataException ex) {
        }

        try {
            defaultMineralStandardModel = getFirstMineralStandardModel();

        } catch (BadLabDataException badLabDataException) {
        }

        try {
            defaultRareEarthElementModel = getFirstRareEarthElementModel();

        } catch (BadLabDataException badLabDataException) {
        }

        // all lab data instances have this as default
        getReportSettingsModels().add(ReportSettings.EARTHTIMEReportSettings());
        try {
            setDefaultreportSettingsModel(getFirstReportSettingsModel());
        } catch (BadLabDataException ex) {
        }

        setDefaultMITValues();

        defaultMineralName = MineralTypes.OTHER.getName();
        defaultSettingType = DataDictionary.SettingType[0];
        defaultNumberOfGrains = 1;
        defaultPhysicallyAbraded = false;
        defaultLeachedInHFAcid = false;
        defaultAnnealedAndChemicallyAbraded = false;
        defaultChemicallyPurifiedUPb = false;

        setDefaultStaceyKramersOnePctUnct(BigDecimal.ONE);

        setDefaultStaceyKramersCorrelationCoeffs(new BigDecimal(0.5));

        // save off initial persistent state serialized file
        try {
            ETSerializer.SerializeObjectToFile(this, getMySerializedName());
        } catch (ETException eTException) {
        }

        this.defaultLeftShadeCountForLAICPMSAquisitions = 0;
    }

    /**
     *
     */
    public void setDefaultMITValues() {
        // set these at MIT values for now
        // updated sept 12 2012 by Noah and Sam Bowring
        setDefaultPbBlankMassInGrams(new ValueModel(
                AnalysisMeasures.pbBlankMassInGrams.getName(),
                new BigDecimal("0.0000000000009"),
                "ABS",
                new BigDecimal("0.0000000000003"), BigDecimal.ZERO));
        setAssumedUBlankMassInGrams(new ValueModel(
                AnalysisMeasures.uBlankMassInGrams.getName(),
                new BigDecimal("0.0000000000001"),
                "ABS",
                new BigDecimal("0.00000000000001"), BigDecimal.ZERO));

        // 0.00205 +/- 0.00002
        setDefaultR18O_16O(new ValueModel(
                AnalysisMeasures.r18O_16O.getName(),
                new BigDecimal("0.00205"),
                "ABS",
                new BigDecimal("0.00002"), BigDecimal.ZERO));

        // 137.81846 +/- 0.02232
        setDefaultR238_235b(new ValueModel(
                AnalysisMeasures.r238_235b.getName(),
                new BigDecimal("137.81846"),
                "ABS",
                new BigDecimal("0.02232"), BigDecimal.ZERO));
        //137.81846 +/- 0.02232
        setDefaultR238_235s(new ValueModel(
                AnalysisMeasures.r238_235s.getName(),
                new BigDecimal("137.81846"),
                "ABS",
                new BigDecimal("0.02232"), BigDecimal.ZERO));
        setDefaultTracerMass(new ValueModel(
                AnalysisMeasures.tracerMassInGrams.getName(),
                new BigDecimal("0.000"),
                "ABS",
                new BigDecimal("0.0001"), BigDecimal.ZERO));
        //3.5 +/- 1
        setDefaultRTh_Umagma(new ValueModel(
                AnalysisMeasures.rTh_Umagma.getName(),
                new BigDecimal("3.5"),
                "ABS",
                new BigDecimal("1.0"), BigDecimal.ZERO));
        setDefaultAr231_235sample(new ValueModel(
                AnalysisMeasures.ar231_235sample.getName(),
                new BigDecimal("1.1"),
                "ABS",
                new BigDecimal("0.1"), BigDecimal.ZERO));

    }

    // used for deserialization
    /**
     *
     * @return
     */
    protected Object readResolve() {

        // march 2012 conversion to new models
        restoreBuiltInModels();

        return this;

    }

    /**
     *
     */
    protected void restoreBuiltInModels() {

        this.initialPbModels = InitialPbModelET.getArrayListOfModels();
        this.mineralStandardModels = MineralStandardUPbModel.getArrayListOfModels();
        this.tracerModels = TracerUPbModel.getArrayListOfModels();
        this.blanks = PbBlankICModel.getArrayListOfModels();
        this.physicalConstantsModels = PhysicalConstantsModel.getArrayListOfModels();
        this.rareEarthElementModels = RareEarthElementsModel.getArrayListOfModels();

    }

    /**
     *
     * @return
     */
    public static String getMySerializedName() {
        String mySerializedName
                = File.separator + System.getProperty("user.home")
                + File.separator + ReduxConstants.myUsersETReduxDataFolderName
                + File.separator + labDataFileName;
        return mySerializedName;
    }

    /**
     *
     * @return
     */
    public static String getLabDataFileName() {
        return labDataFileName;
    }

    /**
     *
     * @return
     */
    public static ReduxLabData getInstance() {
        if (instance == null) {
            instance = (ReduxLabData) ETSerializer.GetSerializedObjectFromFile(ReduxLabData.getMySerializedName());
            if (instance == null) {
                // test for transition to ET_Redux from U-Pb_Redux
                // jan 2015 check if old U-Pb_Redux folder exists and copy file from there
                File dataFolderUPbRedux = new File(
                        File.separator + System.getProperty("user.home") + File.separator + myUsersUPbReduxDataFolderName);
                if (dataFolderUPbRedux.exists()) {
                    try {
                        instance = (ReduxLabData) ETSerializer.GetSerializedObjectFromFile(dataFolderUPbRedux.getCanonicalPath() + File.separator + labDataFileName);
                    } catch (IOException iOException) {
                    }
                }
                if (instance == null) {
                    instance = new ReduxLabData();
                }                
            }
        }
        return instance;
    }

    // tracerModels ***************************************************************
    /**
     *
     * @return
     */
    public ArrayList<AbstractRatiosDataModel> getTracers() {
        return tracerModels;
    }

    /**
     *
     * @return @throws org.earthtime.UPb_Redux.exceptions.BadLabDataException
     */
    public AbstractRatiosDataModel getNoneTracer()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) tracerModels).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getFirstTracer()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) tracerModels).getSecondElement();
    }

    /**
     *
     * @param tracerNameAndVersion
     * @return
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getATracerModel(String tracerNameAndVersion)
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) tracerModels).getAnElement(tracerNameAndVersion);
    }

    /**
     *
     * @param tracerNameAndVersion
     * @return
     * @throws BadLabDataException
     */
    public boolean removeATracer(String tracerNameAndVersion)
            throws BadLabDataException {
        return ((ReduxLabDataList) tracerModels).removeAnElement(tracerNameAndVersion);
    }

    /**
     *
     * @param tracerNameAndVersion
     * @return
     * @throws BadLabDataException
     */
    public boolean containsTracerNameVersion(String tracerNameAndVersion)
            throws BadLabDataException {
        return ((ReduxLabDataList) tracerModels).containsElementName(tracerNameAndVersion);
    }

    /**
     *
     * @param tracer
     */
    public void addTracer(AbstractRatiosDataModel tracer) {
        getTracers().add(tracer);
        Collections.sort(getTracers());
    }

    /**
     *
     * @return @throws BadLabDataException
     */
    public AbstractRatiosDataModel getDefaultLabTracer() throws BadLabDataException {
        // first populate if necessary
        if (defaultLabTracer == null) {
            addTracer(TracerUPbModel.getNoneInstance());
            defaultLabTracer = getFirstTracer();
        } else {
            // detect if legacy default is none and change if possible
            if (defaultLabTracer.equals(getNoneTracer())) {
                defaultLabTracer = getFirstTracer();
            }
        }
        return defaultLabTracer;
    }

    /**
     *
     * @param defaultLabTracer
     */
    public void setDefaultLabTracer(AbstractRatiosDataModel defaultLabTracer) {
        this.defaultLabTracer = defaultLabTracer;
    }

    /**
     *
     * @param tracer
     * @param isVerbose
     */
    public void registerTracer(AbstractRatiosDataModel tracer, boolean isVerbose) {
        if (((ReduxLabDataList) tracerModels).registerElement(tracer, isVerbose)) {
            addTracer(tracer);
        }
    }

    // alpha U Models ***************************************************************
    /**
     *
     * @return
     */
    public ArrayList<ValueModel> getAlphaUModels() {
        return alphaUModels;
    }

    /**
     *
     * @return @throws BadLabDataException
     */
    public ValueModel getNoneAlphaUModel()
            throws BadLabDataException {
        return (ValueModel) ((ReduxLabDataList) alphaUModels).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     */
    public ValueModel getFirstAlphaUModel()
            throws BadLabDataException {
        return (ValueModel) ((ReduxLabDataList) alphaUModels).getSecondElement();
    }

    /**
     *
     * @param name
     * @return
     * @throws BadLabDataException
     */
    public ValueModel getAnAlphaUModel(String name)
            throws BadLabDataException {
        return (ValueModel) ((ReduxLabDataList) alphaUModels).getAnElement(name);
    }

    /**
     *
     * @param name
     * @return
     * @throws BadLabDataException
     */
    public boolean removeAnAlphaUModel(String name)
            throws BadLabDataException {
        return ((ReduxLabDataList) alphaUModels).removeAnElement(name);
    }

    /**
     *
     * @param name
     * @return
     * @throws BadLabDataException
     */
    public boolean containsAlphaUModelName(String name)
            throws BadLabDataException {
        return ((ReduxLabDataList) alphaUModels).containsElementName(name);
    }

    /**
     *
     * @param alphaUModel
     */
    public void addAlphaUModel(ValueModel alphaUModel) {
        getAlphaUModels().add(alphaUModel);
        Collections.sort(getAlphaUModels());
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public ValueModel getDefaultLabAlphaUModel()
            throws BadLabDataException {
        if (defaultLabAlphaUModel == null) {
            addAlphaUModel(new ValueModel(ReduxConstants.NONE));
            setDefaultLabAlphaUModel(getFirstAlphaUModel());
        } else {
            // detect if legacy default is none and change if possible
            if (defaultLabAlphaUModel.equals(getNoneAlphaUModel())) {
                setDefaultLabAlphaUModel(getFirstAlphaUModel());
            }
        }
        return defaultLabAlphaUModel;
    }

    /**
     *
     * @param defaultLabAlphaUModel
     */
    public void setDefaultLabAlphaUModel(ValueModel defaultLabAlphaUModel) {
        this.defaultLabAlphaUModel = defaultLabAlphaUModel;
    }

    /**
     *
     * @param alphaUModel
     * @param isVerbose
     */
    public void registerAlphaUModel(ValueModel alphaUModel, boolean isVerbose) {
        if (alphaUModel != null) {
            if (((ReduxLabDataList) alphaUModels).registerElement(alphaUModel, isVerbose)) {
                addAlphaUModel(alphaUModel);
            }
        }
    }

    // alpha Pb Models ***************************************************************
    /**
     *
     * @return
     */
    public ArrayList<ValueModel> getAlphaPbModels() {
        return alphaPbModels;
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public ValueModel getNoneAlphaPbModel()
            throws BadLabDataException {
        return (ValueModel) ((ReduxLabDataList) alphaPbModels).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     */
    public ValueModel getFirstAlphaPbModel()
            throws BadLabDataException {
        return (ValueModel) ((ReduxLabDataList) alphaPbModels).getSecondElement();
    }

    /**
     *
     * @param name
     * @return
     * @throws BadLabDataException
     */
    public ValueModel getAnAlphaPbModel(String name)
            throws BadLabDataException {
        return (ValueModel) ((ReduxLabDataList) alphaPbModels).getAnElement(name);
    }

    /**
     *
     * @param name
     * @return
     * @throws BadLabDataException
     */
    public boolean removeAnAlphaPbModel(String name)
            throws BadLabDataException {
        return ((ReduxLabDataList) alphaPbModels).removeAnElement(name);
    }

    /**
     *
     * @param name
     * @return
     * @throws BadLabDataException
     */
    public boolean containsAlphaPbModelName(String name)
            throws BadLabDataException {
        return ((ReduxLabDataList) alphaPbModels).containsElementName(name);
    }

    /**
     *
     * @param alphaPbModel
     */
    public void addAlphaPbModel(ValueModel alphaPbModel) {
        getAlphaPbModels().add(alphaPbModel);
        Collections.sort(getAlphaPbModels());
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public ValueModel getDefaultLabAlphaPbModel()
            throws BadLabDataException {
        if (defaultLabAlphaPbModel == null) {
            addAlphaPbModel(new ValueModel(ReduxConstants.NONE));
            setDefaultLabAlphaPbModel(getFirstAlphaPbModel());
        } else {
            // detect if legacy default is none and change if possible
            if (defaultLabAlphaPbModel.equals(getNoneAlphaPbModel())) {
                setDefaultLabAlphaPbModel(getFirstAlphaPbModel());
            }
        }
        return defaultLabAlphaPbModel;
    }

    /**
     *
     * @param defaultLabAlphaPbModel
     */
    public void setDefaultLabAlphaPbModel(ValueModel defaultLabAlphaPbModel) {
        this.defaultLabAlphaPbModel = defaultLabAlphaPbModel;
    }

    /**
     *
     * @param alphaPbModel
     * @param isVerbose
     */
    public void registerAlphaPbModel(ValueModel alphaPbModel, boolean isVerbose) {
        if (alphaPbModel != null) {
            if (((ReduxLabDataList) alphaPbModels).registerElement(alphaPbModel, isVerbose)) {
                addAlphaPbModel(alphaPbModel);
            }
        }
    }

    // pb blanks ***************************************************************
    /**
     *
     * @return
     */
    public ArrayList<AbstractRatiosDataModel> getPbBlanks() {
        return blanks;
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getNonePbBlankModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) blanks).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getFirstPbBlank()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) blanks).getSecondElement();
    }

    /**
     *
     * @param blankName
     * @return
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getAPbBlankModel(String blankName)
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) blanks).getAnElement(blankName);
    }

    /**
     *
     * @param blankName
     * @return
     * @throws BadLabDataException
     */
    public boolean removeABlank(String blankName)
            throws BadLabDataException {
        return ((ReduxLabDataList) blanks).removeAnElement(blankName);
    }

    /**
     *
     * @param blankName
     * @return
     * @throws BadLabDataException
     */
    public boolean containsBlankName(String blankName)
            throws BadLabDataException {
        return ((ReduxLabDataList) blanks).containsElementName(blankName);
    }

    /**
     *
     * @param blank
     */
    public void addBlank(AbstractRatiosDataModel blank) {
        getPbBlanks().add(blank);
        Collections.sort(getPbBlanks());
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getDefaultLabPbBlank()
            throws BadLabDataException {
        if (defaultLabPbBlank == null) {
            addBlank(PbBlankICModel.getNoneInstance());
            setDefaultLabPbBlank(getFirstPbBlank());
        } else {
            // detect if legacy default is none and change if possible
            if (defaultLabPbBlank.equals(getNonePbBlankModel())) {
                setDefaultLabPbBlank(getFirstPbBlank());
            }
        }
        return defaultLabPbBlank;
    }

    /**
     *
     * @param defaultLabPbBlank
     */
    public void setDefaultLabPbBlank(AbstractRatiosDataModel defaultLabPbBlank) {
        this.defaultLabPbBlank = defaultLabPbBlank;
    }

    /**
     *
     * @param pbBlank
     * @param isVerbose
     */
    public void registerPbBlank(AbstractRatiosDataModel pbBlank, boolean isVerbose) {
        if (((ReduxLabDataList) blanks).registerElement(pbBlank, isVerbose)) {
            addBlank(pbBlank);
        }
    }

    // initial pb models ***************************************************************
    /**
     *
     * @return
     */
    public ArrayList<AbstractRatiosDataModel> getInitialPbModels() {
        Collections.sort(initialPbModels);
        return initialPbModels;
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getNoneInitialPbModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) initialPbModels).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getFirstInitialPbModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) initialPbModels).getSecondElement();
    }

    /**
     *
     * @param initialPbModelName
     * @return
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getAnInitialPbModel(String initialPbModelName)
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) initialPbModels).getAnElement(initialPbModelName);
    }

    /**
     *
     * @param intialPbModelName
     * @return
     * @throws BadLabDataException
     */
    public boolean removeAnInitialPbModel(String intialPbModelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) initialPbModels).removeAnElement(intialPbModelName);
    }

    /**
     *
     * @param intialPbModelName
     * @return
     * @throws BadLabDataException
     */
    public boolean containsInitialPbModelName(String intialPbModelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) initialPbModels).containsElementName(intialPbModelName);
    }

    /**
     *
     * @param model
     */
    public void addInitialPbModel(AbstractRatiosDataModel model) {
        initialPbModels.add(model);
        Collections.sort(initialPbModels);
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getDefaultLabInitialPbModel()
            throws BadLabDataException {
        if (defaultLabInitialPbModel == null) {

            addInitialPbModel(InitialPbModelET.getNoneInstance());

            defaultLabInitialPbModel = getFirstInitialPbModel();
        } else {
            // detect if legacy default is none and change if possible
            if (defaultLabInitialPbModel.equals(getNoneInitialPbModel())) {
                defaultLabInitialPbModel = getFirstInitialPbModel();
            }
        }
        return defaultLabInitialPbModel;
    }

    /**
     *
     * @param defaultLabInitialPbModel
     */
    public void setDefaultLabInitialPbModel(AbstractRatiosDataModel defaultLabInitialPbModel) {
        this.defaultLabInitialPbModel = defaultLabInitialPbModel;
    }

    /**
     *
     * @param initialPbModel
     * @param isVerbose
     */
    public void registerInitialPbModel(AbstractRatiosDataModel initialPbModel, boolean isVerbose) {
        if (((ReduxLabDataList) initialPbModels).registerElement(initialPbModel, isVerbose)) {
            addInitialPbModel(initialPbModel);
        }
    }

    // physical constants ***************************************************************
    /**
     *
     * @return
     */
    public ArrayList<AbstractRatiosDataModel> getPhysicalConstantsModels() {
        return physicalConstantsModels;
    }

    /**
     *
     * @return @throws BadLabDataException
     */
    public AbstractRatiosDataModel getNonePhysicalConstantsModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) physicalConstantsModels).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getFirstPhysicalConstantsModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) physicalConstantsModels).getSecondElement();
    }

    /**
     *
     * @param physicalConstantsModelName
     * @return
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getAPhysicalConstantsModel(String physicalConstantsModelName)
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) physicalConstantsModels).getAnElement(physicalConstantsModelName);
    }

    /**
     *
     * @param physicalConstantsModelName
     * @return
     * @throws BadLabDataException
     */
    public boolean removeAPhysicalConstantsModel(String physicalConstantsModelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) physicalConstantsModels).removeAnElement(physicalConstantsModelName);
    }

    /**
     *
     * @param physicalConstantsModelName
     * @return
     * @throws BadLabDataException
     */
    public boolean containsPhysicalConstantsModelNameAndVersion(String physicalConstantsModelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) physicalConstantsModels).containsElementName(physicalConstantsModelName);
    }

    /**
     *
     * @param model
     */
    public void addPhysicalConstantsModel(AbstractRatiosDataModel model) {
        getPhysicalConstantsModels().add(model);
        Collections.sort(getPhysicalConstantsModels());
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getDefaultPhysicalConstantsModel()
            throws BadLabDataException {
        if (defaultPhysicalConstantsModel == null) {
            addPhysicalConstantsModel(PhysicalConstantsModel.getNoneInstance());
            defaultPhysicalConstantsModel = getFirstPhysicalConstantsModel();
        } else {
            // detect if legacy default is none and change if possible
            if (defaultPhysicalConstantsModel.equals(getNonePhysicalConstantsModel())) {
                defaultPhysicalConstantsModel = getFirstPhysicalConstantsModel();
            }
        }
        return defaultPhysicalConstantsModel;
    }

    /**
     *
     * @param defaultPhysicalConstantsModel
     */
    public void setDefaultPhysicalConstantsModel(AbstractRatiosDataModel defaultPhysicalConstantsModel) {
        this.defaultPhysicalConstantsModel = defaultPhysicalConstantsModel;
    }

    /**
     *
     * @param physicalConstantsModel
     * @param isVerbose
     */
    public void registerPhysicalConstantsModel(AbstractRatiosDataModel physicalConstantsModel, boolean isVerbose) {
        if (((ReduxLabDataList) physicalConstantsModels).registerElement(physicalConstantsModel, isVerbose)) {
            addPhysicalConstantsModel(physicalConstantsModel);
        }
    }

    // Mineral Standards models ***************************************************************
    /**
     *
     * @return
     */
    public ArrayList<AbstractRatiosDataModel> getMineralStandardModels() {
        return mineralStandardModels;
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getNoneMineralStandardModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) mineralStandardModels).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getFirstMineralStandardModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) mineralStandardModels).getSecondElement();
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getAMineralStandardModel(String modelName)
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) mineralStandardModels).getAnElement(modelName);
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public boolean removeAMineralStandardModel(String modelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) mineralStandardModels).removeAnElement(modelName);
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public boolean containsMineralStandardModelName(String modelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) mineralStandardModels).containsElementName(modelName);
    }

    /**
     *
     * @param model
     */
    public void addMineralStandardModel(AbstractRatiosDataModel model) {
        mineralStandardModels.add(model);
        Collections.sort(mineralStandardModels);
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getDefaultTIMSMineralStandardModel()
            throws BadLabDataException {
        if (defaultMineralStandardModel == null) {
            addMineralStandardModel(MineralStandardUPbModel.getNoneInstance());
            defaultMineralStandardModel = getFirstMineralStandardModel();
        } else {
            // detect if legacy default is none and change if possible
            if (defaultMineralStandardModel.equals(getNoneMineralStandardModel())) {
                defaultMineralStandardModel = getFirstMineralStandardModel();
            }
        }
        return defaultMineralStandardModel;
    }

    /**
     *
     * @param model
     */
    public void setDefaultTIMSMineralStandardModel(AbstractRatiosDataModel model) {
        this.defaultMineralStandardModel = model;
    }

    /**
     *
     * @param model
     * @param isVerbose
     */
    public void registerMineralStandardModel(AbstractRatiosDataModel model, boolean isVerbose) {
        if (((ReduxLabDataList) mineralStandardModels).registerElement(model, isVerbose)) {
            addMineralStandardModel(model);
        }
    }

    public void setDefaultLAICPMSPrimaryMineralStandardModel(AbstractRatiosDataModel defaultLAICPMSPrimaryMineralStandardModel) {
        this.defaultLAICPMSPrimaryMineralStandardModel = defaultLAICPMSPrimaryMineralStandardModel;
    }

    public AbstractRatiosDataModel getDefaultLAICPMSPrimaryMineralStandardModel()
            throws BadLabDataException {
        if (defaultLAICPMSPrimaryMineralStandardModel == null) {
            defaultLAICPMSPrimaryMineralStandardModel = getFirstMineralStandardModel();
        }
        return defaultLAICPMSPrimaryMineralStandardModel;
    }

    // nov 2014 rare earth elements models
    // Mineral Standards models ***************************************************************
    /**
     *
     * @return
     */
    public ArrayList<AbstractRatiosDataModel> getRareEarthElementModels() {
        return rareEarthElementModels;
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getNoneRareEarthElementModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) rareEarthElementModels).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getFirstRareEarthElementModel()
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) rareEarthElementModels).getSecondElement();
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getARareEarthElementModel(String modelName)
            throws BadLabDataException {
        return (AbstractRatiosDataModel) ((ReduxLabDataList) rareEarthElementModels).getAnElement(modelName);
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public boolean removeARareEarthElementModel(String modelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) rareEarthElementModels).removeAnElement(modelName);
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public boolean containsRareEarthElementName(String modelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) rareEarthElementModels).containsElementName(modelName);
    }

    /**
     *
     * @param model
     */
    public void addRareEarthElementModel(AbstractRatiosDataModel model) {
        rareEarthElementModels.add(model);
        Collections.sort(getRareEarthElementModels());
    }

    /**
     *
     * @return @throws BadLabDataException
     * @throws BadLabDataException
     */
    public AbstractRatiosDataModel getDefaultRareEarthElementModel()
            throws BadLabDataException {
        if (defaultRareEarthElementModel == null) {
            addRareEarthElementModel(RareEarthElementsModel.getNoneInstance());
            defaultRareEarthElementModel = getFirstMineralStandardModel();
        } else {
            // detect if legacy default is none and change if possible
            if (defaultRareEarthElementModel.equals(getNoneMineralStandardModel())) {
                defaultRareEarthElementModel = getFirstMineralStandardModel();
            }
        }
        return defaultRareEarthElementModel;
    }

    /**
     *
     * @param model
     */
    public void setDefaultRareEarthElementModel(AbstractRatiosDataModel model) {
        this.defaultRareEarthElementModel = model;
    }

    /**
     *
     * @param model
     * @param isVerbose
     */
    public void registerRareEarthElementModel(AbstractRatiosDataModel model, boolean isVerbose) {
        if (((ReduxLabDataList) rareEarthElementModels).registerElement(model, isVerbose)) {
            addMineralStandardModel(model);
        }
    }

    // june 2014 report settings models take 2 *********************************
    /**
     *
     * @return
     */
    public ArrayList<ReportSettings> getReportSettingsModels() {
        return reportSettingsModels;
    }

    /**
     *
     * @return @throws BadLabDataException
     */
    public ReportSettings getNoneReportSettingsModel()
            throws BadLabDataException {
        return (ReportSettings) ((ReduxLabDataList) reportSettingsModels).getFirstElement();
    }

    /**
     *
     * @return @throws BadLabDataException
     */
    public ReportSettings getFirstReportSettingsModel()
            throws BadLabDataException {
        return (ReportSettings) ((ReduxLabDataList) reportSettingsModels).getSecondElement();
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public ReportSettings getAReportSettingsModel(String modelName)
            throws BadLabDataException {
        return (ReportSettings) ((ReduxLabDataList) reportSettingsModels).getAnElement(modelName);
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public boolean removeAReportSettingsModel(String modelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) reportSettingsModels).removeAnElement(modelName);
    }

    /**
     *
     * @param modelName
     * @return
     * @throws BadLabDataException
     */
    public boolean containsReportSettingsModelName(String modelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) reportSettingsModels).containsElementName(modelName);
    }

    /**
     *
     * @param model
     */
    public void addReportSettingsModel(ReportSettings model) {
        reportSettingsModels.add(model);
        Collections.sort(reportSettingsModels);
    }

    /**
     *
     * @return @throws BadLabDataException
     */
    public ReportSettings getDefaultReportSettingsModel()
            throws BadLabDataException {
        if (defaultReportSettingsModel == null) {
            defaultReportSettingsModel = ReportSettings.EARTHTIMEReportSettings();
            addReportSettingsModel(defaultReportSettingsModel);
        }

        // new approach oct 2014
        if (defaultReportSettingsModel.isOutOfDate()) {
            String myReportSettingsName = defaultReportSettingsModel.getName();
            defaultReportSettingsModel = new ReportSettings(myReportSettingsName);
        }
        return defaultReportSettingsModel;
    }

    /**
     *
     * @param model
     */
    public void setDefaultreportSettingsModel(ReportSettings model) {
        this.defaultReportSettingsModel = model;
    }

    /**
     *
     * @param model
     * @param isVerbose
     */
    public void registerReportSettingsModel(ReportSettings model, boolean isVerbose) {
        if (((ReduxLabDataList) reportSettingsModels).registerElement(model, isVerbose)) {
            addReportSettingsModel(model);
        }
    }

    // report settings NOT COMPLETED AS OF OCT 2010 ***************************************************************
//    /**
//     *
//     * @return
//     */
//    public ArrayList<ReportSettings> getReportSettingsModels () {
//        if ( reportSettingsModels == null ) {
//            reportSettingsModels =
//                    new ReduxLabDataList<ReportSettings>( "Report Settings" );
//        }
//        return reportSettingsModels;
//    }
//
//    /**
//     *
//     * @return @throws BadLabDataException
//     * @throws BadLabDataException
//     */
//    public ReportSettings getFirstReportSettingsModel ()
//            throws BadLabDataException {
//        
//        ReportSettings myRS = ((ReportSettings) ((ReduxLabDataList) reportSettingsModels).getFirstElement());
//        
//        return myRS;
//
//    }
//
//    /**
//     *
//     * @param reportSettingsModelName
//     * @return
//     * @throws BadLabDataException
//     */
//    public ReportSettings getAReportSettingsModel ( String reportSettingsModelName )
//            throws BadLabDataException {
//        
//                ReportSettings myRS = ((ReportSettings) ((ReduxLabDataList) reportSettingsModels).getFirstElement());
//        myRS.updateToCurrentVersion();
//
//        return myRS;
//
//    }
//
//    /**
//     *
//     * @param reportSettingsModelName
//     * @return
//     * @throws BadLabDataException
//     */
//    public boolean removeAReportSettingsModel ( String reportSettingsModelName )
//            throws BadLabDataException {
//        return ((ReduxLabDataList) reportSettingsModels).removeAnElement( reportSettingsModelName );
//    }
    /**
     *
     * @param reportSettingsModelName
     * @return
     * @throws BadLabDataException
     */
    public boolean containsReportSettingsModelsModelNameAndVersion(String reportSettingsModelName)
            throws BadLabDataException {
        return ((ReduxLabDataList) reportSettingsModels).containsElementName(reportSettingsModelName);
    }

//    /**
//     *
//     * @param model
//     */
//    public void addReportSettingsModel ( ReportSettings model ) {
//        
//        getReportSettingsModels().remove(model);
//        model.updateToCurrentVersion();
//
//
//        getReportSettingsModels().add( model );
//        Collections.sort( getReportSettingsModels() );
//    }
//    /**
//     *
//     * @return @throws BadLabDataException
//     * @throws BadLabDataException
//     */
//    public ReportSettings getDefaultReportSettingsModel ()
//            throws BadLabDataException {
//        //if ( defaultReportSettingsModel == null ) {
//            
//        addReportSettingsModel( ReportSettings.EARTHTIMEReportSettings() );
//        setDefaultreportSettingsModel( getFirstReportSettingsModel() );
//       // }
//
//        return defaultReportSettingsModel;
//    }
//    /**
//     *
//     * @param defaultReportSettingsModel
//     */
//    public void setDefaultreportSettingsModel ( ReportSettings defaultReportSettingsModel ) {
//        
//        defaultReportSettingsModel.updateToCurrentVersion();
//        this.defaultReportSettingsModel = defaultReportSettingsModel;
//    }
//
//    /**
//     *
//     * @param reportSettingsModel
//     * @param isVerbose
//     */
//    public void registerReportSettingsModel ( ReportSettings reportSettingsModel, boolean isVerbose ) {
//        reportSettingsModel.updateToCurrentVersion();
//        if ( ((ReduxLabDataList) reportSettingsModels).registerElement( reportSettingsModel, isVerbose ) ) {
//            addReportSettingsModel( reportSettingsModel );
//        }
//    }
    // properties
    /**
     *
     * @return
     */
    public String getLabName() {
        return labName;
    }

    /**
     *
     * @param labName
     */
    public void setLabName(String labName) {
        this.labName = labName;
    }

    /**
     *
     * @return
     */
    public ValueModel getDefaultPbBlankMassInGrams() {
        return defaultPbBlankMassInGrams;
    }

    /**
     *
     * @param defaultPbBlankMassInGrams
     */
    public void setDefaultPbBlankMassInGrams(ValueModel defaultPbBlankMassInGrams) {
        this.defaultPbBlankMassInGrams = defaultPbBlankMassInGrams;
    }

    /**
     *
     * @return
     */
    public ValueModel getDefaultAssumedUBlankMassInGrams() {
        return assumedUBlankMassInGrams;
    }

    /**
     *
     * @param assumedUBlankMassInGrams
     */
    public void setAssumedUBlankMassInGrams(ValueModel assumedUBlankMassInGrams) {
        this.assumedUBlankMassInGrams = assumedUBlankMassInGrams;
    }

    /**
     *
     * @return
     */
    public ValueModel getDefaultR18O_16O() {
        return defaultR18O_16O;
    }

    /**
     *
     * @param defaultR18O_16O
     */
    public void setDefaultR18O_16O(ValueModel defaultR18O_16O) {
        this.defaultR18O_16O = defaultR18O_16O;
    }

    /**
     *
     * @return
     */
    public ValueModel getDefaultR238_235s() {
        return defaultR238_235s;
    }

    /**
     *
     * @param defaultR238_235s
     */
    public void setDefaultR238_235s(ValueModel defaultR238_235s) {
        this.defaultR238_235s = defaultR238_235s;
    }

    /**
     *
     * @return
     */
    public ValueModel getDefaultR238_235b() {
        return defaultR238_235b;
    }

    /**
     *
     * @param defaultR238_235b
     */
    public void setDefaultR238_235b(ValueModel defaultR238_235b) {
        this.defaultR238_235b = defaultR238_235b;
    }

    /**
     *
     * @return
     */
    public ValueModel getDefaultTracerMass() {
        return defaultTracerMass;
    }

    /**
     *
     * @param defaultTracerMass
     */
    public void setDefaultTracerMass(ValueModel defaultTracerMass) {
        this.defaultTracerMass = defaultTracerMass;
    }

    /**
     *
     * @param fraction
     */
    public void registerFractionWithLabData(Fraction fraction) {

        registerTracer(//
                ((UPbFractionI) fraction).getTracer(), false);

        // may be null if coming from imported aliquot and register will catch it
        registerAlphaPbModel(((UPbFractionI) fraction).getAlphaPbModel(), false);

        // may be null if coming from imported aliquot and register will catch it
        registerAlphaUModel(((UPbFractionI) fraction).getAlphaUModel(), false);

        registerPbBlank(
                ((UPbFractionI) fraction).getPbBlank(), false);
        registerInitialPbModel(
                ((UPbFractionI) fraction).getInitialPbModel(), false);
        registerPhysicalConstantsModel(
                ((UPbFractionI) fraction).getPhysicalConstantsModel(), false);

        // TODO register mineral standards, reportsettings
        // but these are not fraction-level
    }

    /**
     * @return the defaultRTh_Umagma
     */
    public ValueModel getDefaultRTh_Umagma() {
        // dec 2008 upgrade
        if (defaultRTh_Umagma == null) {
            setDefaultRTh_Umagma(new ValueModel(
                    AnalysisMeasures.rTh_Umagma.getName(),
                    new BigDecimal("2.8"),
                    "ABS",
                    new BigDecimal("0.0"), BigDecimal.ZERO));
        }
        return defaultRTh_Umagma;
    }

    /**
     * @param defaultRTh_Umagma the defaultRTh_Umagma to set
     */
    public void setDefaultRTh_Umagma(ValueModel defaultRTh_Umagma) {
        this.defaultRTh_Umagma = defaultRTh_Umagma;
    }

    /**
     * @return the defaultAr231_235sample
     */
    public ValueModel getDefaultAr231_235sample() {
        // dec 2008 upgrade
        if (defaultAr231_235sample == null) {
            setDefaultAr231_235sample(new ValueModel(
                    AnalysisMeasures.ar231_235sample.getName(),
                    new BigDecimal("1.5"),
                    "ABS",
                    new BigDecimal("0.0"), BigDecimal.ZERO));
        }
        return defaultAr231_235sample;
    }

    /**
     * @param defaultAr231_235sample the defaultAr231_235sample to set
     */
    public void setDefaultAr231_235sample(ValueModel defaultAr231_235sample) {
        this.defaultAr231_235sample = defaultAr231_235sample;
    }

    /**
     * @return the defaultStaceyKramersOnePctUnct
     */
    public BigDecimal getDefaultStaceyKramersOnePctUnct() {
        if (defaultStaceyKramersOnePctUnct == null) {
            defaultStaceyKramersOnePctUnct = BigDecimal.ONE;
        }
        return defaultStaceyKramersOnePctUnct;
    }

    /**
     * @param defaultStaceyKramersOnePctUnct the defaultStaceyKramersOnePctUnct
     * to set
     */
    public void setDefaultStaceyKramersOnePctUnct(BigDecimal defaultStaceyKramersOnePctUnct) {
        this.defaultStaceyKramersOnePctUnct = defaultStaceyKramersOnePctUnct;
    }

    /**
     * @return the defaultStaceyKramersCorrelationCoeffs
     */
    public BigDecimal getDefaultStaceyKramersCorrelationCoeffs() {
        if (defaultStaceyKramersCorrelationCoeffs == null) {
            defaultStaceyKramersCorrelationCoeffs = new BigDecimal(0.5);
        }
        return defaultStaceyKramersCorrelationCoeffs;
    }

    /**
     * @param defaultStaceyKramersCorrelationCoeffs the
     * defaultStaceyKramersCorrelationCoeffs to set
     */
    public void setDefaultStaceyKramersCorrelationCoeffs(BigDecimal defaultStaceyKramersCorrelationCoeffs) {
        this.defaultStaceyKramersCorrelationCoeffs = defaultStaceyKramersCorrelationCoeffs;
    }

    private static String toolTipForAlphaModel(String alphaType, ValueModel alphaModel) {
        String retVal = "<html>" + alphaModel.getName() + "<br>";
        retVal += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + alphaType + " = " //
                + alphaModel.getValue().movePointRight(2)//
                .setScale(ReduxConstants.DEFAULT_PARAMETERS_SCALE, RoundingMode.HALF_UP).toPlainString()//
                + " % / amu" + "<br>"//
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1-sigma abs = " //
                + alphaModel.getOneSigma().movePointRight(2)//
                .setScale(ReduxConstants.DEFAULT_PARAMETERS_SCALE, RoundingMode.HALF_UP).toPlainString()//
                + " % / amu" + "</html>";

        return retVal;
    }

    /**
     *
     * @param alphaPbModel
     * @return
     */
    public static String toolTipForAlphaPbModel(ValueModel alphaPbModel) {
        return toolTipForAlphaModel(AnalysisMeasures.alphaPb.getName(), alphaPbModel);
    }

    /**
     *
     * @param alphaUModel
     * @return
     */
    public static String toolTipForAlphaUModel(ValueModel alphaUModel) {
        return toolTipForAlphaModel(AnalysisMeasures.alphaU.getName(), alphaUModel);
    }

    /**
     *
     * @param tracer
     * @return
     */
    public static String toolTipForTracer(AbstractRatiosDataModel tracer) {
        String retVal = "<html>" + tracer.getReduxLabDataElementName() + "<br>";
        retVal += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + ((TracerUPbModel) tracer).getTracerType()//
                + "</html>";

        return retVal;
    }

    // lab equipment settings ****************************************
    /**
     * @return the labEquipmentSettings
     */
    public ArrayList<LabEquipmentSettings> getLabEquipmentSettings() {
        return labEquipmentSettings;
    }

    /**
     * @param labEquipmentSettings the labEquipmentSettings to set
     */
    public void setLabEquipmentSettings(ArrayList<LabEquipmentSettings> labEquipmentSettings) {
        this.labEquipmentSettings = labEquipmentSettings;
    }

    /**
     *
     * @return
     */
    public LabEquipmentSettings getNoneLabEquipmentSettings() {
        return getModelNone(labEquipmentSettings);
    }

    /**
     *
     * @return
     */
    public LabEquipmentSettings getFirstLabEquipmentSettings() {
        return getModelFirst(labEquipmentSettings);
    }

    // generics Oct 2010 *******************************************************
    /**
     *
     * @param <T>
     * @param U
     * @return
     */
    public <T> T getModelNone(ArrayList<T> U) {
        T retVal = null;
        try {
            retVal = (T) ((ReduxLabDataList<T>) U).getFirstElement();
        } catch (BadLabDataException badLabDataException) {
        }

        return retVal;
    }

    /**
     *
     * @param <T>
     * @param U
     * @return
     */
    public <T> T getModelFirst(ArrayList<T> U) {
        T retVal = null;
        try {
            retVal = (T) ((ReduxLabDataList<T>) U).getSecondElement();
        } catch (BadLabDataException badLabDataException) {
        }

        return retVal;
    }

    /**
     * @return the analystName
     */
    public String getAnalystName() {
        if (analystName == null) {
            analystName = System.getProperty("user.name");
        }
        return analystName;
    }

    /**
     * @param analystName the analystName to set
     */
    public void setAnalystName(String analystName) {
        this.analystName = analystName;
    }

//    public Tracer getATracerModel ( String tracerNameandVersion )
//            throws BadLabDataException {
//        return (Tracer) ((ReduxLabDataList) tracerModels).getAnElement( tracerNameandVersion );
//    }
//
//    public boolean removeATracer ( String tracerNameandVersion )
//            throws BadLabDataException {
//        return ((ReduxLabDataList) tracerModels).removeAnElement( tracerNameandVersion );
//    }
//
//    public boolean containsTracerNameVersion ( String tracerNameandVersion )
//            throws BadLabDataException {
//        return ((ReduxLabDataList) tracerModels).containsElementName( tracerNameandVersion );
//    }
//
//    public void addTracer ( Tracer tracer ) {
//        getTracers().add( tracer );
//        Collections.sort( getTracers() );
//    }
//
//    public Tracer getDefaultLabTracer () throws BadLabDataException {
//        // first populate if necessary
//        if ( defaultLabTracer == null ) {
//            addTracer( new Tracer( "<none>" ) );
//            setDefaultLabTracer( getFirstTracer() );
//        } else {
//            // detect if legacy default is none and change if possible
//            if ( defaultLabTracer.equals( getNoneTracer() ) ) {
//                setDefaultLabTracer( getFirstTracer() );
//            }
//        }
//        return defaultLabTracer;
//    }
//
//    public void setDefaultLabTracer ( Tracer defaultLabTracer ) {
//        this.defaultLabTracer = defaultLabTracer;
//    }
//
//    public void registerTracer ( Tracer tracer, boolean isVerbose ) {
//        if ( ((ReduxLabDataList) tracerModels).registerElement( tracer, isVerbose ) ) {
//            addTracer( tracer );
//        }
//    }
    /**
     * @return the defaultMineralName
     */
    public String getDefaultMineralName() {
        if (defaultMineralName == null) {
            defaultMineralName = MineralTypes.OTHER.getName();
        }
        return defaultMineralName;
    }

    /**
     * @param defaultMineralName the defaultMineralName to set
     */
    public void setDefaultMineralName(String defaultMineralName) {
        this.defaultMineralName = defaultMineralName;
    }

    /**
     * @return the defaultSettingType
     */
    public String getDefaultSettingType() {
        if (defaultSettingType == null) {
            defaultSettingType = DataDictionary.SettingType[0];
        }
        return defaultSettingType;
    }

    /**
     * @param defaultSettingType the defaultSettingType to set
     */
    public void setDefaultSettingType(String defaultSettingType) {
        this.defaultSettingType = defaultSettingType;
    }

    /**
     * @return the defaultNumberOfGrains
     */
    public int getDefaultNumberOfGrains() {
        return defaultNumberOfGrains;
    }

    /**
     * @param defaultNumberOfGrains the defaultNumberOfGrains to set
     */
    public void setDefaultNumberOfGrains(int defaultNumberOfGrains) {
        this.defaultNumberOfGrains = defaultNumberOfGrains;
    }

    /**
     * @return the defaultPhysicallyAbraded
     */
    public boolean isDefaultPhysicallyAbraded() {
        return defaultPhysicallyAbraded;
    }

    /**
     * @param defaultPhysicallyAbraded the defaultPhysicallyAbraded to set
     */
    public void setDefaultPhysicallyAbraded(boolean defaultPhysicallyAbraded) {
        this.defaultPhysicallyAbraded = defaultPhysicallyAbraded;
    }

    /**
     * @return the defaultLeachedInHFAcid
     */
    public boolean isDefaultLeachedInHFAcid() {
        return defaultLeachedInHFAcid;
    }

    /**
     * @param defaultLeachedInHFAcid the defaultLeachedInHFAcid to set
     */
    public void setDefaultLeachedInHFAcid(boolean defaultLeachedInHFAcid) {
        this.defaultLeachedInHFAcid = defaultLeachedInHFAcid;
    }

    /**
     * @return the defaultAnnealedAndChemicallyAbraded
     */
    public boolean isDefaultAnnealedAndChemicallyAbraded() {
        return defaultAnnealedAndChemicallyAbraded;
    }

    /**
     * @param defaultAnnealedAndChemicallyAbraded the
     * defaultAnnealedAndChemicallyAbraded to set
     */
    public void setDefaultAnnealedAndChemicallyAbraded(boolean defaultAnnealedAndChemicallyAbraded) {
        this.defaultAnnealedAndChemicallyAbraded = defaultAnnealedAndChemicallyAbraded;
    }

    /**
     * @return the defaultChemicallyPurifiedUPb
     */
    public boolean isDefaultChemicallyPurifiedUPb() {
        return defaultChemicallyPurifiedUPb;
    }

    /**
     * @param defaultChemicallyPurifiedUPb the defaultChemicallyPurifiedUPb to
     * set
     */
    public void setDefaultChemicallyPurifiedUPb(boolean defaultChemicallyPurifiedUPb) {
        this.defaultChemicallyPurifiedUPb = defaultChemicallyPurifiedUPb;
    }

    /**
     * @return the defaultLeftShadeCountForLAICPMSAquisitions
     */
    public int getDefaultLeftShadeCountForLAICPMSAquisitions() {
        return defaultLeftShadeCountForLAICPMSAquisitions;
    }

    /**
     * @param defaultLeftShadeCountForLAICPMSAquisitions the
     * defaultLeftShadeCountForLAICPMSAquisitions to set
     */
    public void setDefaultLeftShadeCountForLAICPMSAquisitions(int defaultLeftShadeCountForLAICPMSAquisitions) {
        this.defaultLeftShadeCountForLAICPMSAquisitions = defaultLeftShadeCountForLAICPMSAquisitions;
    }
}
