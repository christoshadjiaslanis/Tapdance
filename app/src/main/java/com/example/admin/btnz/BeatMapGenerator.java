package com.example.admin.btnz;

import android.util.DisplayMetrics;

import com.badlogic.gdx.audio.analysis.FFT;
import com.example.admin.btnz.GameComponents.Btns.HitCircle;
import com.example.admin.btnz.GameComponents.Difficulty;

import java.io.FileInputStream;
import java.util.ArrayList;


import javazoom.jl.decoder.*;


/**
 * Created by Admin on 3/4/2016.
 */
public class BeatMapGenerator {

    public static final int THRESHOLD_WINDOW_SIZE = 2;//5
    public static final int FFT_SIZE = 32; // 2^N
    public static int samplingFrequency = 44100; //HZ
    public static float msPerFrame;

    public static final double SCALE = 4;
    public static final int SEGMENTATION_FACTOR = 32;
    public static float multiplier = 1.6f;
    public static long minimumDivisibleTime = 160;

    public static boolean invert = false;


    public static ArrayList<HitCircle> generateBeatMap(FileInputStream fileInputStream, DisplayMetrics displayMetrics, int durationMillis, Difficulty difficulty) throws BitstreamException {


        setDifficulty(difficulty);


        //TODO ONLY DOES ONE CHANNEL
        //TODO ATTEMPT WITH THREADING TO REDUCE LOAD TIMES-


        //TODO Consider reapplying hamming?
        System.out.println(displayMetrics.widthPixels);

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
        System.out.println("Done Initiating in :" + String.valueOf(System.currentTimeMillis() - startTime));
        int k = 0;
        boolean noteof = true;

        short pcmBuffer[];
        SampleBuffer buffer;
        ArrayList<Double> spectra = new ArrayList<Double>();

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
                        k++;
                    } else {
                        noteof = false;
                    }


                }
            } catch (DecoderException e) {
                System.out.println(e.getMessage());
            }
        } catch (BitstreamException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Done Decoding in :" + String.valueOf(System.currentTimeMillis() - startTime));
        System.out.println(k);
        System.out.println(spectralFlux.size());

        for (int i = 0; i < spectralFlux.size(); i++) {
            int start = Math.max(0, i - THRESHOLD_WINDOW_SIZE);
            int end = Math.min(spectralFlux.size() - 1, i + THRESHOLD_WINDOW_SIZE);
            float mean = 0;
            for (int j = start; j <= end; j++)
                mean += spectralFlux.get(j);
            mean /= (end - start);
            threshold.add(mean * multiplier);
        }


        for (int i = 0; i < threshold.size(); i++) {
            if (threshold.get(i) <= spectralFlux.get(i))
                prunnedSpectralFlux.add(spectralFlux.get(i) - threshold.get(i));
            else
                prunnedSpectralFlux.add((float) 0);
        }

        for (int i = 0; i < prunnedSpectralFlux.size() - 1; i++) {

            if (prunnedSpectralFlux.get(i) > prunnedSpectralFlux.get(i + 1)) {
                peaks.add(prunnedSpectralFlux.get(i));

            } else
                peaks.add((float) 0);
        }
        System.out.println(prunnedSpectralFlux.size());

        float z = 1.0000000f;
        ArrayList<HitCircle> beatMap = new ArrayList<HitCircle>();
        double tLast = 0;
        double dt;

        int prevX = displayMetrics.widthPixels / 2;
        int prevY = displayMetrics.heightPixels / 2;
        double theta;

        HitCircle hitCircle;
        long timeInMillis = 0;

        //Testing
        HitCircle.setRadiusInPixels(displayMetrics);
        //Testing

        for (int i = 0; i < peaks.size(); i++) {
            if (peaks.get(i) > 0) {
                timeInMillis = (long) ((((float) i * msPerFrame)));
                dt = (long) ((timeInMillis) - tLast);
                if ((dt > minimumDivisibleTime) && (timeInMillis > 2000)) {
                    if(Math.log(spectra.get(i)) > 9) {
                        theta = generateTheta(spectra.get(i));
                        //System.out.println(spectra.get(i));
                        prevX = generateX(dt, prevX, theta, displayMetrics.widthPixels, HitCircle.getRadiusInPixels());
                        prevY = generateY(dt, prevY, theta, displayMetrics.heightPixels, HitCircle.getRadiusInPixels());
                        hitCircle = new HitCircle(timeInMillis, 1000, prevX, prevY, z);
                        hitCircle.normalise(displayMetrics);
                        beatMap.add(hitCircle);

                        tLast = timeInMillis;

                        z -= 0.0000001f;
                    }

                }
            }

        }
        return beatMap;


    }


    private static float[] floatMe(short[] pcms) {

        float[] floaters = new float[FFT_SIZE];
        for (int i = 0; i < FFT_SIZE; i++) {
            floaters[i] = pcms[i];
        }
        return floaters;
    }


    //TODO replace constant in generateX with DisplayMetrics equivalent

    private static int generateX(double dt, int prevX, double theta, int width, int radiusInPixels) {
        int x = 0;

        double distanceMetric = SCALE * dt;

        distanceMetric = distanceMetric * (1/multiplier) * 1.8;

        double dx = Math.cos(theta) * distanceMetric;

        x = (int) Math.abs(prevX + dx);
        x = x % width;
        x = x - (x % (radiusInPixels));

        return x;

    }

    private static int generateY(double dt, int prevY, double theta, int height, int radiusInPixels) {
        int y = 0;

        double distanceMetric = SCALE * dt;

        //test

        distanceMetric = distanceMetric * (1/multiplier) * 1.8;

        double dy = (Math.sin(theta) * distanceMetric);
        y = (int) Math.abs(prevY + dy);
        y = y % height;
        y = y - (y % (radiusInPixels));

        return y;
    }

    private static double generateTheta(double spectrumMax) {

        double theta = spectrumMax / 100000 * 2 * Math.PI;
        theta = theta - (theta % (2 * Math.PI / SEGMENTATION_FACTOR));
        System.out.println(Math.log(spectrumMax));

        return theta;

    }

    private static void setDifficulty(Difficulty difficulty) {

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
