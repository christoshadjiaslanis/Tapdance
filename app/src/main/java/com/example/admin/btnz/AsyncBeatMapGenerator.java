
package com.example.admin.btnz;

import android.app.Activity;
import android.os.Looper;
import android.util.DisplayMetrics;

import com.badlogic.gdx.audio.analysis.FFT;
import com.example.admin.btnz.GameComponents.Btns.HitCircle;
import com.example.admin.btnz.GameComponents.Difficulty;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 * Created by Admin on 12/7/2016.
*/
public class AsyncBeatMapGenerator {


    public static final int THRESHOLD_WINDOW_SIZE = 20; //5
    public static final int FFT_SIZE = 32; // 2^N
    public static int samplingFrequency = 44100; //HZ
    public float msPerFrame;

    public static final double SCALE = 4;
    public final int SEGMENTATION_FACTOR = 32;
    public float multiplier = 1.6f;
    public long minimumDivisibleTime = 160;

    public static boolean stopped = false;


    public AsyncBeatMapGenerator() {


    }


    public void generateBeatMap(FileInputStream fileInputStream, DisplayMetrics displayMetrics, int durationMillis, Difficulty difficulty, ArrayList<HitCircle> beatMap, GameScreen gameScreen) throws BitstreamException {


        setDifficulty(difficulty);

        final GameScreen finalGameScreen = gameScreen;


        float[] samples = new float[1024];
        float[] spectrum = new float[FFT_SIZE / 2 + 1];
        float[] lastSpectrum = new float[FFT_SIZE / 2 + 1];
        ArrayList<Float> spectralFlux = new ArrayList<Float>();
        ArrayList<Float> threshold = new ArrayList<Float>();
        ArrayList<Float> prunnedSpectralFlux = new ArrayList<Float>();
        ArrayList<Float> peaks = new ArrayList<Float>();

        long startTime = System.currentTimeMillis();

        Bitstream bitStream = new Bitstream(fileInputStream);
        javazoom.jl.decoder.Decoder decoder = new javazoom.jl.decoder.Decoder();
        //System.out.println("Done Initiating in :" + String.valueOf(System.currentTimeMillis() - startTime));
        int k = 0;
        boolean noteof = true;

        short pcmBuffer[];
        SampleBuffer buffer;
        ArrayList<Double> spectra = new ArrayList<Double>();


        float z = 10.0f;
        float a = 0f;

        double tLast = 0;
        double dt;

        int prevX = displayMetrics.widthPixels / 2;
        int prevY = displayMetrics.heightPixels / 2;
        double theta;

        HitCircle hitCircle;
        long timeInMillis = 0;

        stopped = false;
        //Testing
        HitCircle.setRadiusInPixels(displayMetrics);
        //Testing

        Looper.prepare();
        try {

            Header frameHeader = bitStream.readFrame();


            samplingFrequency = frameHeader.frequency();
            msPerFrame = frameHeader.ms_per_frame();

            FFT fft = new FFT(FFT_SIZE, samplingFrequency);

            try {

                while (noteof) {
                    frameHeader = bitStream.readFrame();


                    if (frameHeader != null) {


                        buffer = (SampleBuffer) decoder.decodeFrame(frameHeader, bitStream);
                        pcmBuffer = buffer.getBuffer();
                        samples = floatMe(pcmBuffer);
                        bitStream.closeFrame();

                        fft.forward(samples);
                        System.arraycopy(spectrum, 0, lastSpectrum, 0, spectrum.length);
                        System.arraycopy(fft.getSpectrum(), 0, spectrum, 0, spectrum.length);
                        double spectraElement = 0;

                        float flux = 0;
                        for (int i = 0; i < spectrum.length; i++) {
                            float value = (spectrum[i] - lastSpectrum[i]);
                            //spectraElement += (Math.log(spectrum[i]));
                            spectraElement += spectrum[i];
                            flux += value < 0 ? 0 : value;
                        }

                        spectraElement /= spectrum.length;
                        spectra.add(spectraElement);
                        spectralFlux.add(flux);
                        threshold.clear();
                        int threshold_n = 0;

                        if(spectralFlux.size() > 1000){
                            threshold_n = spectralFlux.size() - 999;
                        }else{
                            threshold_n = 0;
                        }

                        for (int i = threshold_n; i < spectralFlux.size(); i++) {
                            int start = Math.max(0, i - THRESHOLD_WINDOW_SIZE);
                            int end = Math.min(spectralFlux.size() - 1, i + THRESHOLD_WINDOW_SIZE);
                            float mean = 0;
                            for (int j = start; j <= end; j++)
                                mean += spectralFlux.get(j);
                            mean /= (end - start);
                            threshold.add(mean * multiplier);
                        }

                        //System.out.println("spectral--------------------");
                        // System.out.println(threshold.size());
                        // System.out.println(spectralFlux.size());
                        prunnedSpectralFlux.clear();
                        for (int i = 0; i < threshold.size(); i++) {
                            if (threshold.get(i) <= spectralFlux.get( threshold_n + i))
                                prunnedSpectralFlux.add(spectralFlux.get(threshold_n + i) - threshold.get(i));
                            else
                                prunnedSpectralFlux.add((float) 0);
                        }

                        // System.out.println("prunned--------------------");
                        peaks.clear();
                        for (int i = 0; i < prunnedSpectralFlux.size() - 1; i++) {

                            if (prunnedSpectralFlux.get(prunnedSpectralFlux.size() - 2) > prunnedSpectralFlux.get(prunnedSpectralFlux.size() - 1)) {
                                peaks.add(prunnedSpectralFlux.get(i));

                            } else
                                peaks.add((float) 0);
                        }
                        //System.out.println("peaks--------------------");
                        if (stopped) {
                            stopped = false;
                            return;
                        }
                        if (peaks.size() > 0) {
                            try {

                                //System.out.println("peaks.get(k)-------------" + peaks.get(k - 1));
                                if (peaks.get(k - 1 - threshold_n) > 0) {
                                    timeInMillis = (long) ((((float) (k - 1) * msPerFrame)));
                                    dt = (long) ((timeInMillis) - tLast);
                                    if ((dt > minimumDivisibleTime) && (timeInMillis > 2000)) {
                                        if (Math.log(spectra.get(k - 1 - threshold_n)) > 9) {
                                            theta = generateTheta(spectra.get(k - 1));
                                            //System.out.println(spectra.get(k - 1));
                                            prevX = generateX(dt, prevX, theta, displayMetrics.widthPixels, HitCircle.getRadiusInPixels());
                                            prevY = generateY(dt, prevY, theta, displayMetrics.heightPixels, HitCircle.getRadiusInPixels());

                                            a += 0.0001f;
                                            //  System.out.println(z - a);
                                             System.out.println("Actual: " + (System.currentTimeMillis() - startTime));
                                             System.out.println("Processed: " + timeInMillis);
                                            System.out.println("-------------------");
                                            hitCircle = new HitCircle(timeInMillis - (System.currentTimeMillis() - startTime), 1000, prevX, prevY, z - a);
                                            hitCircle.normalise(displayMetrics);
                                            final HitCircle finalHitCircle = hitCircle;
                                            gameScreen.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    finalGameScreen.addHitCircle(finalHitCircle);

                                                }
                                            });

                                            tLast = timeInMillis;

                                        }

                                    }
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }

                        k++;

                    } else {
                        System.out.println("finished");
                        noteof = false;
                    }


                }
            } catch (DecoderException e) {
                System.out.println(e.getMessage());
            }
        } catch (BitstreamException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Total time taken:" + (System.currentTimeMillis() - startTime));
    }


    private static float[] floatMe(short[] pcms) {

        float[] floaters = new float[FFT_SIZE];
        for (int i = 0; i < FFT_SIZE; i++) {
            floaters[i] = pcms[i];
        }
        return floaters;
    }


    //TODO replace constant in generateX with DisplayMetrics equivalent

    private int generateX(double dt, int prevX, double theta, int width, int radiusInPixels) {
        int x = 0;

        double distanceMetric = SCALE * dt;

        distanceMetric = distanceMetric * (1 / multiplier) * 1.8;

        double dx = Math.cos(theta) * distanceMetric;

        x = (int) Math.abs(prevX + dx);
        x = x % width;
        x = x - (x % (radiusInPixels));

        return x;

    }

    private int generateY(double dt, int prevY, double theta, int height, int radiusInPixels) {
        int y = 0;

        double distanceMetric = SCALE * dt;

        //test

        distanceMetric = distanceMetric * (1 / multiplier) * 1.8;

        double dy = (Math.sin(theta) * distanceMetric);
        y = (int) Math.abs(prevY + dy);
        y = y % height;
        y = y - (y % (radiusInPixels));

        return y;
    }

    private double generateTheta(double spectrumMax) {

        double theta = spectrumMax / 100000 * 2 * Math.PI;
        theta = theta - (theta % (2 * Math.PI / SEGMENTATION_FACTOR));
        //System.out.println(Math.log(spectrumMax));
        return theta;

    }

    private void setDifficulty(Difficulty difficulty) {

        switch (difficulty) {
            case INSANE: {
                multiplier = 1.15f;
                minimumDivisibleTime = 110;

                break;
            }
            case HARD: {

                multiplier = 1.3f;
                minimumDivisibleTime = 130;
                break;
            }

            case MEDIUM: {
                multiplier = 1.5f;
                minimumDivisibleTime = 160;
                break;
            }

            case EASY: {
                multiplier = 1.8f;
                minimumDivisibleTime = 250;
                break;
            }


        }

    }


}

