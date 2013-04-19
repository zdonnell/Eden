package com.zdonnell.eve;

import java.text.DecimalFormat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.priceservice.PriceService;
import com.zdonnell.eve.staticdata.api.StaticData;
import com.zdonnell.eve.staticdata.api.TypeInfo;

public class TypeInfoFragment extends Fragment {	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{		
		setRetainInstance(true);
		final Integer typeID = getArguments().getInt("typeID");
		
		/* Setup the GridView properties and link with the CursorAdapater */
		View mainView = (View) inflater.inflate(R.layout.type_info, container, false);
		
		final ImageView typeIcon = (ImageView) mainView.findViewById(R.id.type_info_typeIcon);
		
		final TextView typeName = (TextView) mainView.findViewById(R.id.type_info_name);
		final TextView typeValue = (TextView) mainView.findViewById(R.id.type_info_value);
		final TextView typeM3 = (TextView) mainView.findViewById(R.id.type_info_m3);
		final TextView typeDescription = (TextView) mainView.findViewById(R.id.type_info_description);
		
		ImageService.getInstance(inflater.getContext()).getTypes(new ImageService.IconObtainedCallback() 
		{
			@Override
			public void iconsObtained(SparseArray<Bitmap> bitmaps) {
				typeIcon.setImageBitmap(bitmaps.get(typeID));
			}
		}, false, typeID);
		
		new StaticData(inflater.getContext()).getTypeInfo(new APICallback<SparseArray<TypeInfo>>((BaseActivity) getActivity()) 
		{
			@Override
			public void onUpdate(SparseArray<TypeInfo> typeInfo) 
			{
				typeName.setText(typeInfo.get(typeID).typeName);
				typeDescription.setText(typeInfo.get(typeID).description);
				typeDescription.setMovementMethod(new ScrollingMovementMethod());
				
				DecimalFormat twoDForm = new DecimalFormat("#,###");				
				typeM3.setText(twoDForm.format(typeInfo.get(typeID).m3) + " m3");
			}
		}, typeID);
		
		PriceService.getInstance(inflater.getContext()).getValues(new Integer[] { typeID }, new APICallback<SparseArray<Float>>((BaseActivity) getActivity()) 
		{
			@Override
			public void onUpdate(SparseArray<Float> prices) 
			{
				DecimalFormat twoDForm = new DecimalFormat("#,###.##");				
				typeValue.setText(twoDForm.format(prices.get(typeID)) + " ISK");
			}
		});
		
		
		return mainView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}
}