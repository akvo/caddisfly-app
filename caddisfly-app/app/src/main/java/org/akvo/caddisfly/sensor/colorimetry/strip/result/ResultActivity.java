/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.strip.result;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.ColorDetected;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.TestTypeListActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.CircleFillView;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ResultActivity extends BaseActivity {

    private final JSONObject resultJsonObj = new JSONObject();
    private final JSONArray resultJsonArr = new JSONArray();
    private Mat resultImage = null;
    private FileStorage fileStorage;
    private String resultImageUrl;
    private StripTest.Brand brand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        setTitle(R.string.result);

        if (savedInstanceState == null) {
            resultImageUrl = UUID.randomUUID().toString() + ".png";
            Intent intent = getIntent();
            fileStorage = new FileStorage(this);
            String brandName = intent.getStringExtra(Constant.BRAND);

            Mat strip;
            StripTest stripTest = new StripTest();

            // get information on the strip test from JSON
            brand = stripTest.getBrand(brandName);
            List<StripTest.Brand.Patch> patches = brand.getPatches();

            // get the JSON describing the images of the patches that were stored before
            JSONArray imagePatchArray = null;
            try {
                String json = fileStorage.readFromInternalStorage(Constant.IMAGE_PATCH + ".txt");
                if (json != null) {
                    imagePatchArray = new JSONArray(json);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // cycle over the patches and interpret them
            if (imagePatchArray != null) {
                // if this strip is of type 'GROUP', take the first image and use that for all the patches
                if (brand.getGroupingType() == StripTest.GroupType.GROUP) {
                    // handle grouping case

                    // get the first patch image
                    JSONArray array;
                    try {
                        // get strip image into Mat object
                        array = imagePatchArray.getJSONArray(0);
                        int imageNo = array.getInt(0);
                        boolean isInvalidStrip = fileStorage.checkIfFilenameContainsString(Constant.STRIP + imageNo + Constant.ERROR);
                        strip = ResultUtil.getMatFromFile(fileStorage, imageNo);
                        if (strip != null) {
                            // create empty mat to serve as a template
                            resultImage = new Mat(0, strip.cols(), CvType.CV_8UC3, new Scalar(255, 255, 255));
                            new BitmapTask(isInvalidStrip, strip, true, brand, patches, 0).execute(strip);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // if this strip is of type 'INDIVIDUAL' handle patch by patch
                    for (int i = 0; i < patches.size(); i++) { // handle patch
                        JSONArray array;
                        try {
                            array = imagePatchArray.getJSONArray(i);

                            // get the image number from the json array
                            int imageNo = array.getInt(0);
                            boolean isInvalidStrip = fileStorage.checkIfFilenameContainsString(Constant.STRIP + imageNo + Constant.ERROR);

                            // read strip from file
                            strip = ResultUtil.getMatFromFile(fileStorage, imageNo);

                            if (strip != null) {
                                if (i == 0) {
                                    // create empty mat to serve as a template
                                    resultImage = new Mat(0, strip.cols(), CvType.CV_8UC3, new Scalar(255, 255, 255));
                                }
                                new BitmapTask(isInvalidStrip, strip, false, brand, patches, i).execute(strip);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText(R.string.noData);
                LinearLayout layout = (LinearLayout) findViewById(R.id.layout_results);

                layout.addView(textView);
            }
        }
        Button save = (Button) findViewById(R.id.button_save);
        Button redo = (Button) findViewById(R.id.button_cancel);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "";
                try {
                    // store image on sd card
                    path = FileStorage.writeBitmapToExternalStorage(ResultUtil.makeBitmap(resultImage), "/result-images", resultImageUrl);

                    resultJsonObj.put(SensorConstants.TYPE, SensorConstants.TYPE_NAME);
                    resultJsonObj.put(SensorConstants.NAME, brand.getName());
                    resultJsonObj.put(SensorConstants.UUID, brand.getUuid());
                    if (path.length() > 0) {
                        resultJsonObj.put(SensorConstants.IMAGE, resultImageUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                listener.onResult(resultJsonObj.toString(), path);

                Intent intent = new Intent(getIntent());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileStorage != null) {
                    fileStorage.deleteFromInternalStorage(Constant.INFO);
                    fileStorage.deleteFromInternalStorage(Constant.DATA);
                    fileStorage.deleteFromInternalStorage(Constant.STRIP);
                }

                Intent intentRedo = new Intent(getBaseContext(), TestTypeListActivity.class);
                intentRedo.putExtra(SensorConstants.FINISH, true);
                intentRedo.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intentRedo);
                finish();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private class BitmapTask extends AsyncTask<Mat, Void, Void> {
        private final boolean invalid;
        private final Boolean grouped;
        private final StripTest.Brand brand;
        private final List<StripTest.Brand.Patch> patches;
        private final int patchNum;
        private final Mat strip;
        String unit;
        int id;
        String patchDescription;
        private Bitmap stripBitmap = null;
        private Mat combined;
        private ColorDetected colorDetected;
        private ColorDetected[] colorsDetected;
        private double resultValue = -1;

        public BitmapTask(boolean invalid, Mat strip, Boolean grouped, StripTest.Brand brand,
                          List<StripTest.Brand.Patch> patches, int patchNum) {
            this.invalid = invalid;
            this.grouped = grouped;
            this.strip = strip;
            this.brand = brand;
            this.patches = patches;
            this.patchNum = patchNum;
        }

        @Override
        protected Void doInBackground(Mat... params) {
            Mat mat = params[0];
            int subMatSize = 7;
            int borderSize = (int) Math.ceil(mat.height() * 0.5);

            if (mat.empty() || mat.height() < subMatSize) {
                return null;
            }

            if (invalid) {
                if (!mat.empty()) {
                    //done with lab schema, make rgb to show in image view
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);
                    stripBitmap = ResultUtil.makeBitmap(mat);
                }
                return null;
            }

            // get the name and unit of the patch
            patchDescription = patches.get(patchNum).getDesc();
            unit = patches.get(patchNum).getUnit();
            id = patches.get(patchNum).getId();

            // depending on the boolean grouped, we either handle all patches at once, or we handle only a single one

            JSONArray colours;
            Point centerPatch = null;

            double xTranslate;

            // compute location of point to be sampled
            if (grouped) {
                // collect colours
                double ratioW = strip.width() / brand.getStripLength();
                colorsDetected = new ColorDetected[patches.size()];
                double[][] colorsValueLab = new double[patches.size()][3];
                for (int p = 0; p < patches.size(); p++) {
                    double x = patches.get(p).getPosition() * ratioW;
                    double y = strip.height() / 2;
                    centerPatch = new Point(x, y);

                    colorDetected = ResultUtil.getPatchColour(mat, centerPatch, subMatSize);
                    double[] colorValueLab = colorDetected.getLab().val;

                    colorsDetected[p] = colorDetected;
                    colorsValueLab[p] = colorValueLab;
                }

                try {
                    resultValue = ResultUtil.calculateResultGroup(colorsValueLab, patches);
                } catch (Exception e) {
                    e.printStackTrace();
                    resultValue = Double.NaN;
                }

                // calculate size of each color range block
                // divide the original strip width by the number of colours
                colours = patches.get(0).getColours();
                xTranslate = (double) mat.cols() / (double) colours.length();

            } else {
                double ratioW = strip.width() / brand.getStripLength();
                double x = patches.get(patchNum).getPosition() * ratioW;
                double y = strip.height() / 2;
                centerPatch = new Point(x, y);

                colorDetected = ResultUtil.getPatchColour(mat, centerPatch, subMatSize);
                double[] colorValueLab = colorDetected.getLab().val;

                //set the colours needed to calculate resultValue
                colours = patches.get(patchNum).getColours();

                try {
                    resultValue = ResultUtil.calculateResultSingle(colorValueLab, colours);
                } catch (Exception e) {
                    e.printStackTrace();
                    resultValue = Double.NaN;
                }

                // calculate size of each color range block
                // divide the original strip width by the number of colours
                xTranslate = (double) mat.cols() / (double) colours.length();
            }

            ////////////// Create Image ////////////////////

            // create Mat to hold strip itself
            mat = ResultUtil.createStripMat(mat, borderSize, centerPatch, grouped);

            // Create Mat to hold patchDescription of patch
            Mat descMat = ResultUtil.createDescriptionMat(patchDescription, mat.cols());

            // Create Mat to hold the colour range
            Mat colorRangeMat;
            if (grouped) {
                colorRangeMat = ResultUtil.createColourRangeMatGroup(patches, mat.cols(), xTranslate);
            } else {
                colorRangeMat = ResultUtil.createColourRangeMatSingle(patches, patchNum, mat.cols(), xTranslate);
            }

            // create Mat to hold value measured
            Mat valueMeasuredMat;
            if (grouped) {
                valueMeasuredMat = ResultUtil.createValueMeasuredMatGroup(colours, resultValue, colorsDetected, mat.cols(), xTranslate);
            } else {
                valueMeasuredMat = ResultUtil.createValueMeasuredMatSingle(colours, resultValue, colorDetected, mat.cols(), xTranslate);
            }

            // PUTTING IT ALL TOGETHER
            // transform all mats to RGB. The strip Mat is already RGB
            Imgproc.cvtColor(descMat, descMat, Imgproc.COLOR_Lab2RGB);
            Imgproc.cvtColor(colorRangeMat, colorRangeMat, Imgproc.COLOR_Lab2RGB);
            Imgproc.cvtColor(valueMeasuredMat, valueMeasuredMat, Imgproc.COLOR_Lab2RGB);

            // create empty mat to serve as a template
            combined = new Mat(0, mat.cols(), CvType.CV_8UC3, new Scalar(255, 255, 255));

            combined = ResultUtil.concatenate(combined, mat); // add strip
            combined = ResultUtil.concatenate(combined, colorRangeMat); // add color range
            combined = ResultUtil.concatenate(combined, valueMeasuredMat); // add measured value

            //make bitmap to be rendered on screen, which doesn't contain the patch patchDescription
            if (!combined.empty()) {
                stripBitmap = ResultUtil.makeBitmap(combined);
            }

            //add patchDescription of patch to combined mat, at the top
            combined = ResultUtil.concatenate(descMat, combined);

            //make bitmap to be send to server
            if (!combined.empty()) {
                resultImage = ResultUtil.concatenate(resultImage, combined);
            }

            //put resultValue in resultJsonArr
            if (!combined.empty()) {
                try {
                    JSONObject object = new JSONObject();
                    object.put(SensorConstants.NAME, patchDescription);
                    object.put(SensorConstants.VALUE, ResultUtil.roundSignificant(resultValue));
                    object.put(SensorConstants.UNIT, unit);
                    object.put(SensorConstants.ID, id);
                    resultJsonArr.put(object);
                    resultJsonObj.put(SensorConstants.RESULT, resultJsonArr);

                    //TESTING write image string to external storage
                    //FileStorage.writeLogToSDFile("base64.txt", img, false);
//                    FileStorage.writeLogToSDFile("json.txt", resultJsonObj.toString(), false);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // End making mats to put into image to be send back as an String to server

            return null;
        }

        /*
        * Puts the result on screen.
        * data is taken from the globals stripBitmap, resultValue, colorDetected and unit variables
        */
        protected void onPostExecute(Void result) {
            LayoutInflater inflater = (LayoutInflater) ResultActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final ViewGroup nullParent = null;
            LinearLayout itemResult = (LinearLayout) inflater.inflate(R.layout.item_result, nullParent, false);

            TextView textTitle = (TextView) itemResult.findViewById(R.id.text_title);
            textTitle.setText(patchDescription);

            ImageView imageResult = (ImageView) itemResult.findViewById(R.id.image_result);
            CircleFillView circleColor = (CircleFillView) itemResult.findViewById(R.id.circle_color);

            if (stripBitmap != null) {
                imageResult.setImageBitmap(stripBitmap);

                if (!invalid) {
                    if (colorDetected != null && !grouped) {
                        circleColor.setColor(colorDetected.getColor());
                    }

                    TextView textView = (TextView) itemResult.findViewById(R.id.text_result);
                    if (resultValue > -1) {
                        if (resultValue < 1.0) {
                            textView.setText(String.format(Locale.getDefault(), "%.2f %s", resultValue, unit));
                        } else {
                            textView.setText(String.format(Locale.getDefault(), "%.1f %s", resultValue, unit));
                        }
                    }
                }
            } else {
                textTitle.append("\n\n" + getResources().getString(R.string.no_data));
                circleColor.setColor(Color.RED);
            }

            LinearLayout layout = (LinearLayout) findViewById(R.id.layout_results);
            layout.addView(itemResult);
        }
    }
}