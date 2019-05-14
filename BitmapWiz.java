/*
 * Copyright (c) 2019, Sivasankaran KB 
 */

package com.sivasankarankb.android_util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import in.gamoz.penbot.util.ExceptionLogger;

public class BitmapWiz {
    // Can read upto 1KB of headers after InputStream.mark().
    // Should be more than enough! Mark is automatic before KITKAT.
    private static final int MARK_HEADER_READ_LIMIT = 1024;

    private Bitmap bSource;
    private BitmapFactory.Options options;
    private Paint paint;

    /**
     * Resets the Bitmap Factory Options field to preferred defaults.
     */
    private void resetOptions(){
        options = new BitmapFactory.Options();
        options.inPreferQualityOverSpeed = true;
        options.inMutable = true;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    /**
     * The simplest.
     */
    public BitmapWiz() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        bSource = null;
        resetOptions();
    }

    /**
     * Clone.
     * @param bitmapWiz The instance to clone
     */
    public BitmapWiz(BitmapWiz bitmapWiz){
        this();
        this.bSource=bitmapWiz.getMutableBitmap();
    }

    /**
     * Start from an image stored in memory.
     * @param memImage The buffer containing the image
     */
    public BitmapWiz(byte [] memImage) {
        this();
        bSource = BitmapFactory.decodeByteArray(memImage, 0,memImage.length, options);
    }

    /**
     * Load an image from a path.
     * @param path The path to an image
     */
    public BitmapWiz(String path) {
        this();
        try {
            FileInputStream inputStream=new FileInputStream(path);
            bSource = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
        } catch (Exception e) {
        }
    }

    /**
     * Load an image from a path and scale the shorter side. Preserves the aspect ratio.
     * @param path The path to an image
     * @param side The size of the shorter side in pixels
     */
    public BitmapWiz(String path, int side) {
        this();
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;

            FileInputStream inputStream=new FileInputStream(path);
            BitmapFactory.decodeStream(inputStream, null, opt);
            inputStream.close();

            int shorterSide;
            boolean lessWidth;

            if (opt.outWidth < opt.outHeight) {
                shorterSide = opt.outWidth;
                lessWidth = true;
            } else {
                shorterSide = opt.outHeight;
                lessWidth = false;
            }

            resetOptions();

            if (side > 0 && shorterSide > side)
                options.inSampleSize = (int) (0.5f + (float) shorterSide / side);

            inputStream=new FileInputStream(path);
            bSource = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            options.inSampleSize = 1;

            if (lessWidth) scale(side, 0);
            else scale(0, side);

        } catch (Exception e) {
            ExceptionLogger.LogStackTrace("BitmapWiz",e);
        } finally {
            options.inSampleSize = 1;
        }
    }

    /**
     * Load an image from a path and scale it to a certain width and height.
     * Set either dimension to zero to automatically calculate it, preserving the aspect ratio.
     * @param path The path to an image
     * @param width The width to scale to
     * @param height The height to scale to
     */
    public BitmapWiz(String path, int width, int height) {
        this();
        try {

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;

            FileInputStream inputStream=new FileInputStream(path);
            BitmapFactory.decodeStream(inputStream, null, opt);
            inputStream.close();

            int widthFactor = 1, heightFactor = 1;

            if (width > 0 && opt.outWidth > width)
                widthFactor = (int) (0.5f + (float) opt.outWidth / width);
            if (height > 0 && opt.outHeight > height)
                heightFactor = (int) (0.5f + (float) opt.outHeight / height);

            resetOptions();

            if (width > 0 && widthFactor < heightFactor) options.inSampleSize = widthFactor;
            else options.inSampleSize = heightFactor;

            inputStream=new FileInputStream(path);
            bSource = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            options.inSampleSize = 1;
            scale(width, height);

        } catch (Exception e) {
        } finally {
            options.inSampleSize = 1;
        }
    }

    /**
     * Load an image from a resource.
     * @param resourceID The ID of the resource to load
     * @param resources The resources object to use (Hint : Call getResources())
     */
    public BitmapWiz(int resourceID, Resources resources) {
        this();
        try {
            bSource = BitmapFactory.decodeResource(resources, resourceID, options);
        } catch (Exception e) {
        }
    }

    /**
     * Load an image from a resource, scale the smaller side. Preserve the aspect ratio.
     * @param resourceID The ID of the resource to load
     * @param resources The resources object to use
     * @param side The size in pixels to scale the shorter side to
     */
    public BitmapWiz(int resourceID, Resources resources, int side) {
        this();
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(resources, resourceID, opt);

            int shorterSide;
            boolean lessWidth;

            if (opt.outWidth < opt.outHeight) {
                shorterSide = opt.outWidth;
                lessWidth = true;
            } else {
                shorterSide = opt.outHeight;
                lessWidth = false;
            }

            resetOptions();

            if (side > 0 && shorterSide > side)
                options.inSampleSize = (int) (0.5f + (float) shorterSide / side);

            bSource = BitmapFactory.decodeResource(resources, resourceID, options);
            options.inSampleSize = 1;

            if (lessWidth) scale(side, 0);
            else scale(0, side);

        } catch (Exception e) {
        } finally {
            options.inSampleSize = 1;
        }
    }

    public BitmapWiz(int resourceID, Resources resources, int width, int height) {
        this();
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(resources, resourceID, opt);

            int widthFactor = 1, heightFactor = 1;

            if (width > 0 && opt.outWidth > width)
                widthFactor = (int) (0.5f + (float) opt.outWidth / width);
            if (height > 0 && opt.outHeight > height)
                heightFactor = (int) (0.5f + (float) opt.outHeight / height);

            resetOptions();

            if (width > 0 && widthFactor < heightFactor) options.inSampleSize = widthFactor;
            else options.inSampleSize = heightFactor;

            bSource = BitmapFactory.decodeResource(resources, resourceID, options);
            options.inSampleSize = 1;
            scale(width, height);
        } catch (Exception e) {
        } finally {
            options.inSampleSize = 1;
        }
    }

    public BitmapWiz scale(int width, int height) {

        if(bSource==null) return this;

        if (width <= 0 && height <= 0) return this;
        if (width == bSource.getWidth() && height == bSource.getHeight()) return this;
        if (width == bSource.getWidth() && height <= 0) return this;
        if (width <= 0 && height == bSource.getHeight()) return this;

        // Automatic width or height
        if (width <= 0) width = (int) ((float) height * bSource.getWidth() / bSource.getHeight());
        else if (height <= 0) height = (int) ((float) width * bSource.getHeight() / bSource.getWidth());

        Bitmap bScaled = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bScaled);

        canvas.drawBitmap(
                bSource,
                new Rect(0, 0, bSource.getWidth(), bSource.getHeight()),
                new Rect(0, 0, width, height),
                paint
        );

        bSource = bScaled;
        return this;
    }

    // Crop to a square
    public BitmapWiz squareCrop() {
        int dim;
        Rect rect, rect1;

        if(bSource==null) return this;

        if (bSource.getWidth() < bSource.getHeight()) { // Calculate crop rectangle
            dim = bSource.getWidth();
            int off = (bSource.getHeight() - dim) >>> 1;
            rect = new Rect(0, off, dim, dim + off);
        } else {
            dim = bSource.getHeight();
            int off = (bSource.getWidth() - dim) >>> 1;
            rect = new Rect(off, 0, dim + off, dim);
        }

        rect1 = new Rect(0, 0, dim, dim);
        Bitmap bCropped = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bCropped);
        canvas.drawBitmap(bSource, rect, rect1, paint);

        bSource = bCropped;
        return this;
    }

    public BitmapWiz circleCrop() {
        float dim, cX, cY;

        if(bSource==null) return this;

        cX = bSource.getWidth() >>> 1;
        cY = bSource.getHeight() >>> 1;

        // Center in image
        if (bSource.getWidth() > bSource.getHeight()) { dim = cY;}
        else { dim = cX; }

        Path circlePath = new Path();
        circlePath.addCircle(cX, cY, dim, Path.Direction.CW);
        circlePath.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        // Cut out transfer mode
        Paint pPaint = new Paint(paint);
        pPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        pPaint.setStyle(Paint.Style.FILL);
        pPaint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));

        Canvas canvas = new Canvas(bSource);
        canvas.drawPath(circlePath, pPaint);

        return this;
    }

    /**
     * Get a copy of the internal bitmap.
     *
     * @return An immutable copy of the bitmap.
     */
    public Bitmap getBitmap() {
        if(bSource==null) return null;
        return Bitmap.createBitmap(bSource);
    }

    /**
     * Get a mutable copy of the internal bitmap.
     *
     * @return A copy of the bitmap.
     */
    public Bitmap getMutableBitmap() {
        if(bSource==null) return null;

        Bitmap bCopy = Bitmap.createBitmap(bSource.getWidth(), bSource.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bCopy);
        Rect rect = new Rect(0, 0, bSource.getWidth(), bSource.getHeight());

        canvas.drawBitmap(bSource, rect, rect, paint);
        return bCopy;
    }

    public boolean saveAsPNG(String path){
        boolean stat;
        if(bSource==null) return false;

        try{
            FileOutputStream output=new FileOutputStream(path);
            stat=bSource.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
        }catch (Exception e){return false;}

        return stat;
    }

    public boolean saveAsPNG(File file){
        boolean stat;
        if(bSource==null) return false;

        try{
            FileOutputStream output=new FileOutputStream(file);
            stat=bSource.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
        }catch (Exception e){return false;}

        return stat;
    }

    public boolean saveAsPNG(FileOutputStream file){
        if(bSource==null) return false;
        return bSource.compress(Bitmap.CompressFormat.PNG, 100, file);
    }

    public boolean saveAsJPEG(String path, int quality){
        boolean stat;
        if(bSource==null) return false;

        try{
            FileOutputStream output=new FileOutputStream(path);
            stat=bSource.compress(Bitmap.CompressFormat.JPEG, quality, output);
            output.close();
        }catch (Exception e){return false;}

        return stat;
    }

    public boolean saveAsJPEG(File file, int quality){
        boolean stat;
        if(bSource==null) return false;

        try{
            FileOutputStream output=new FileOutputStream(file);
            stat=bSource.compress(Bitmap.CompressFormat.JPEG, quality, output);
            output.close();
        }catch (Exception e){return false;}

        return stat;
    }

    public boolean saveAsJPEG(FileOutputStream file, int quality){
        if(bSource==null) return false;
        return bSource.compress(Bitmap.CompressFormat.JPEG, quality, file);
    }

    public byte [] saveAsJPEG(int quality){
        if(bSource==null) return null;

        try{
            ByteArrayOutputStream output=new ByteArrayOutputStream();
            if(bSource.compress(Bitmap.CompressFormat.JPEG, quality, output))
                return output.toByteArray();
            else
                return null;
        }catch (Exception e){return null;}
    }
}
