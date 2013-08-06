package com.lanouveller.franceexpatries;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class ItemGuides extends TextView {

	private Paint linePaint;

	public ItemGuides(Context context, AttributeSet ats, int ds) 
	{
		super(context, ats, ds);
		init();
	}

	public ItemGuides(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init();
	}

	public ItemGuides(Context context) 
	{
		super(context);
		init();
	}

	public void init() 
	{
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(getResources().getColor(R.color.gray));
	}
	
	@Override
	public void onDraw(Canvas canvas) 
	{
		int debutX = 0 + getMeasuredWidth() / 8;
		int finX = getMeasuredWidth() - getMeasuredWidth() / 8;
		int debutY = getMeasuredHeight();
		int finY = getMeasuredHeight();

		canvas.drawLine(debutX, debutY, finX, finY, linePaint);

		super.onDraw(canvas);
	}
}
