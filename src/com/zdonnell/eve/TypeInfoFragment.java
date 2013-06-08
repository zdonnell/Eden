package com.zdonnell.eve;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zdonnell.eve.apilink.APICallback;
import com.zdonnell.eve.helpers.ImageURL;
import com.zdonnell.eve.priceservice.PriceService;
import com.zdonnell.eve.staticdata.StaticData;
import com.zdonnell.eve.staticdata.TypeInfo;

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
		
		ImageLoader.getInstance().displayImage(ImageURL.forType(typeID), typeIcon);
		
		new StaticData(inflater.getContext()).getTypeInfo(new APICallback<SparseArray<TypeInfo>>((ILoadingActivity) getActivity()) 
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
		
		PriceService.getInstance(inflater.getContext()).getValues(new Integer[] { typeID }, new APICallback<SparseArray<Float>>((ILoadingActivity) getActivity()) 
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