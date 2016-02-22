package com.guardswift.ui.view.card;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.prototypes.CardWithList;

/**
 * Created by cyrixmorten on 21/02/16.
 */
public class ReportCard extends CardWithList {

    public ReportCard(Context context) {
        super(context);
    }

    @Override
    protected CardHeader initCardHeader() {
        //Add Header
        CardHeader header = new CardHeader(getContext());
        //Add a popup menu. This method set OverFlow button to visible
//        header.setPopupMenu(R.menu.extras_popupmain, new CardHeader.OnClickCardHeaderPopupMenuListener() {
//            @Override
//            public void onMenuItemClick(BaseCard card, MenuItem item) {
//                Toast.makeText(getContext(), "Click on " + item.getTitle(), Toast.LENGTH_SHORT).show();
//            }
//        });
        header.setTitle("Report"); //should use R.string.
        return header;
    }

    @Override
    protected void initCard() {
//        setEmptyViewViewStubLayoutId(R.layout.carddemo_extras_base_withlist_empty);
    }

    @Override
    protected List<ListObject> initChildren() {
        return null;
    }

    @Override
    public View setupChildView(int childPosition, ListObject object, View convertView, ViewGroup parent) {

        //Setup the elements inside each row
        TextView dayText = (TextView) convertView.findViewById(R.id.carddemo_weather_dayName);
        TextView dayDate = (TextView) convertView.findViewById(R.id.carddemo_weather_dayDate);
        ImageView icon = (ImageView) convertView.findViewById(R.id.carddemo_weather_dayIcon);
        TextView minTempText = (TextView) convertView.findViewById(R.id.carddemo_weather_dayTempMin);
        TextView maxTempText = (TextView) convertView.findViewById(R.id.carddemo_weather_dayTempMax);
        TextView dayDescr = (TextView) convertView.findViewById(R.id.carddemo_weather_dayDescr);

        WeatherObject weatherObject= (WeatherObject)object;
        Date d = new Date();
        Calendar gc =  new GregorianCalendar();
        gc.setTime(d);
        gc.add(GregorianCalendar.DAY_OF_MONTH, childPosition + 1);
        dayText.setText(sdfDay.format(gc.getTime()));
        dayDate.setText(sdfMonth.format(gc.getTime()));

        icon.setImageResource(WeatherIconMapper.getWeatherResource(weatherObject.mDayForecast.weather.currentCondition.getIcon(), weatherObject.mDayForecast.weather.currentCondition.getWeatherId()));
        Log.d("SwA", "Min [" + minTempText + "]");

        minTempText.setText( Math.round(weatherObject.mDayForecast.forecastTemp.min) + units.tempUnit);
        maxTempText.setText( Math.round(weatherObject.mDayForecast.forecastTemp.max) + units.tempUnit);
        dayDescr.setText(weatherObject.mDayForecast.weather.currentCondition.getDescr());

        return convertView;
    }

    @Override
    public int getChildLayoutId() {
        return 0;
    }
}
