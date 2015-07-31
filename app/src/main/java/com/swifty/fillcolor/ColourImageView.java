package com.swifty.fillcolor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.Random;
import java.util.Stack;

public class ColourImageView extends ImageView {

    private Bitmap mBitmap;
    /**
     * �߽����ɫ
     */
    private int mBorderColor = -1;

    private boolean hasBorderColor = false;

    private Stack<Point> mStacks = new Stack<Point>();

    public ColourImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ColourImageView);
        mBorderColor = ta.getColor(R.styleable.ColourImageView_border_color, -1);
        hasBorderColor = (mBorderColor != -1);

        L.e("hasBorderColor = " + hasBorderColor + " , mBorderColor = " + mBorderColor);

        ta.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();

        //�Կ��Ϊ��׼���ȱ�������view�ĸ߶�
        setMeasuredDimension(viewWidth,
                getDrawable().getIntrinsicHeight() * viewWidth / getDrawable().getIntrinsicWidth());
        L.e("view's width = " + getMeasuredWidth() + " , view's height = " + getMeasuredHeight());

        //����drawable��ȥ�õ�һ����viewһ����С��bitmap
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        Bitmap bm = drawable.getBitmap();
        mBitmap = Bitmap.createScaledBitmap(bm, getMeasuredWidth(), getMeasuredHeight(), false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //��ɫ
                fillColorToSameArea(x, y);
            case MotionEvent.ACTION_MOVE:
                L.e(x + "," + y);
                fillColorToSameArea(x, y);
        }
        return super.onTouchEvent(event);
    }

    /**
     * ����x,y��øĵ���ɫ���������
     *
     * @param x
     * @param y
     */
    private void fillColorToSameArea(int x, int y) {
        Bitmap bm = mBitmap;

        int pixel = bm.getPixel(x, y);
        if (pixel == Color.TRANSPARENT || (hasBorderColor && mBorderColor == pixel)) {
            return;
        }
        int newColor = randomColor();

        int w = bm.getWidth();
        int h = bm.getHeight();
        //�õ���bitmap����ɫ����
        int[] pixels = new int[w * h];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        //��ɫ
        fillColor(pixels, w, h, pixel, newColor, x, y);
        //��������bitmap
        bm.setPixels(pixels, 0, w, 0, 0, w, h);
        setImageDrawable(new BitmapDrawable(bm));

    }

    /**
     * @param pixels   ��������
     * @param w        ���
     * @param h        �߶�
     * @param pixel    ��ǰ�����ɫ
     * @param newColor ���ɫ
     * @param i        ������
     * @param j        ������
     */
    private void fillColor(int[] pixels, int w, int h, int pixel, int newColor, int i, int j) {
        //����1�������ӵ�(x, y)��ջ��
        mStacks.push(new Point(i, j));

        //����2���ж�ջ�Ƿ�Ϊ�գ�
        // ���ջΪ��������㷨������ȡ��ջ��Ԫ����Ϊ��ǰɨ���ߵ����ӵ�(x, y)��
        // y�ǵ�ǰ��ɨ���ߣ�
        while (!mStacks.isEmpty()) {


            /**
             * ����3�������ӵ�(x, y)�������ص�ǰɨ��������������������䣬
             * ֱ���߽硣�ֱ������ε����Ҷ˵�����ΪxLeft��xRight��
             */
            Point seed = mStacks.pop();
            //L.e("seed = " + seed.x + " , seed = " + seed.y);
            int count = fillLineLeft(pixels, pixel, w, h, newColor, seed.x, seed.y);
            int left = seed.x - count + 1;
            count = fillLineRight(pixels, pixel, w, h, newColor, seed.x + 1, seed.y);
            int right = seed.x + count;


            /**
             * ����4��
             * �ֱ����뵱ǰɨ�������ڵ�y - 1��y + 1����ɨ����������[xLeft, xRight]�е����أ�
             * ��xRight��ʼ��xLeft��������������ɨ�������ΪAAABAAC��AΪ���ӵ���ɫ����
             * ��ô��B��Cǰ���A��Ϊ���ӵ�ѹ��ջ�У�Ȼ�󷵻صڣ�2������
             */
            //��y-1������
            if (seed.y - 1 >= 0)
                findSeedInNewLine(pixels, pixel, w, h, seed.y - 1, left, right);
            //��y+1������
            if (seed.y + 1 < h)
                findSeedInNewLine(pixels, pixel, w, h, seed.y + 1, left, right);
        }


    }

    /**
     * �����������ӽڵ�
     *
     * @param pixels
     * @param pixel
     * @param w
     * @param h
     * @param i
     * @param left
     * @param right
     */
    private void findSeedInNewLine(int[] pixels, int pixel, int w, int h, int i, int left, int right) {
        /**
         * ��ø��еĿ�ʼ����
         */
        int begin = i * w + left;
        /**
         * ��ø��еĽ�������
         */
        int end = i * w + right;

        boolean hasSeed = false;

        int rx = -1, ry = -1;

        ry = i;

        /**
         * ��end��begin���ҵ����ӽڵ���ջ��AAABAAAB����Bǰ��AΪ���ӽڵ㣩
         */
        while (end >= begin) {
            if (pixels[end] == pixel) {
                if (!hasSeed) {
                    rx = end % w;
                    mStacks.push(new Point(rx, ry));
                    hasSeed = true;
                }
            } else {
                hasSeed = false;
            }
            end--;
        }
    }

    /**
     * ������ɫ���������ĸ���
     *
     * @return
     */
    private int fillLineRight(int[] pixels, int pixel, int w, int h, int newColor, int x, int y) {
        int count = 0;

        while (x < w) {
            //�õ�����
            int index = y * w + x;
            if (needFillPixel(pixels, pixel, index)) {
                pixels[index] = newColor;
                count++;
                x++;
            } else {
                break;
            }

        }

        return count;
    }


    /**
     * ������ɫ��������ɫ������ֵ
     *
     * @return
     */
    private int fillLineLeft(int[] pixels, int pixel, int w, int h, int newColor, int x, int y) {
        int count = 0;
        while (x >= 0) {
            //���������
            int index = y * w + x;

            if (needFillPixel(pixels, pixel, index)) {
                pixels[index] = newColor;
                count++;
                x--;
            } else {
                break;
            }

        }
        return count;
    }

    //override by swifty if pixel is white then fill
    private boolean needFillPixel(int[] pixels, int pixel, int index) {
        if (hasBorderColor) {
            return pixels[index] != mBorderColor;
        } else {
//          return pixels[index] == pixel;
            return pixels[index] > 0xFFBBBBBB ? true : false;
        }

    }

    /**
     * ����һ�������ɫ
     *
     * @return
     */
    private int randomColor() {
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        return color;
    }
}