package com.example.testbinomteh;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private MyLocationNewOverlay locationOverlay;
    List<GeoPoint> geoPoints = new ArrayList<>(); // Список всех маркеров
    private int currentMarkerIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        GeoPoint point1 = new GeoPoint(55.753400, 37.612603); // манеж у кремля Моховая улица, 18
        GeoPoint point2 = new GeoPoint(55.749474, 37.613476); // кремль оружейная палата


        geoPoints.add(point1);
        geoPoints.add(point2);

        setContentView(R.layout.activity_main);

        ImageView getLocation = findViewById(R.id.getLocation);
        ImageView zoomPlus = findViewById(R.id.zoomPlus);
        ImageView zoomMinus = findViewById(R.id.zoomMinus);
        ImageView nextMarker = findViewById(R.id.nextTracker);


        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setVisibility(View.VISIBLE);
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        //точка на карте по которой строится карта
        IMapController mapController = map.getController();
        mapController.setZoom(17.0);
        GeoPoint startPoint = new GeoPoint(55.7517, 37.6176);
        mapController.setCenter(startPoint);

        //крутилка карты пальцами
        var mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);

        //наложение шкалы масштаба
        final DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        map.getOverlays().add(mScaleBarOverlay);

        // Если разрешения нет, запрашиваем его у пользователя, если есть, показываем геопозицию
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            enableMyLocationOverlay();
        }
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapController.animateTo(locationOverlay.getMyLocation());
            }
        });

        // Zoom карты
        zoomPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getController().zoomIn();
            }
        });

        zoomMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getController().zoomOut();
            }
        });

        //маркеры

        View customMarkerView1 = getLayoutInflater().inflate(R.layout.castle_marker_layout, null);
        Bitmap customMarkerBitmap1 = createBitmapFromView(customMarkerView1);
        Bitmap scaledBitmapMarker1 = Bitmap.createScaledBitmap(customMarkerBitmap1, 128, 128, false);
        Drawable customMarkerDrawable1 = new BitmapDrawable(getResources(), scaledBitmapMarker1);

        View customMarkerView2 = getLayoutInflater().inflate(R.layout.home_marker_layout, null);
        Bitmap customMarkerBitmap2 = createBitmapFromView(customMarkerView2);
        Bitmap scaledBitmapMarker2 = Bitmap.createScaledBitmap(customMarkerBitmap2, 128, 128, false);
        Drawable customMarkerDrawable2 = new BitmapDrawable(getResources(), scaledBitmapMarker2);

        Marker marker1 = new Marker(map);
        marker1.setPosition(point1);
        marker1.setIcon(customMarkerDrawable2);
        marker1.setInfoWindow(null);
        marker1.setTitle("Манеж,\nЦентральный выставочный зал ");

        Marker marker2 = new Marker(map);
        marker2.setPosition(point2);
        marker2.setIcon(customMarkerDrawable1);
        marker2.setInfoWindow(null);
        marker2.setTitle("Кремль,\nОужейная палата");

        // Добавляем маркеры на карту
        map.getOverlays().add(marker1);
        map.getOverlays().add(marker2);

        marker1.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showBottomSheet(
                        marker.getTitle(),
                        getString(R.string.description1),
                        AppCompatResources.getDrawable(ctx, R.drawable.home));
                return true;
            }
        });

        marker2.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showBottomSheet(
                        marker.getTitle(),
                        getString(R.string.description2),
                        AppCompatResources.getDrawable(ctx, R.drawable.castle));
                return true;
            }
        });

        //переход к следующему маркеру
        nextMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveNextMarker(mapController);
            }
        });
    }

    private void moveNextMarker(IMapController mapController) {
        if (geoPoints != null && geoPoints.size() > 0) {
            currentMarkerIndex++;
            if (currentMarkerIndex >= geoPoints.size()) {
                currentMarkerIndex = 0;
            }
            GeoPoint currentMarker = geoPoints.get(currentMarkerIndex);
            mapController.animateTo(currentMarker);
        }
    }

    private void showBottomSheet(String title, String description, Drawable icon) {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        TextView titleTextView = bottomSheetView.findViewById(R.id.title);
        TextView descriptionTextView = bottomSheetView.findViewById(R.id.description);
        ImageView iconImageView = bottomSheetView.findViewById(R.id.icon);
        iconImageView.setImageDrawable(icon);
        titleTextView.setText(title);
        descriptionTextView.setText(description);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private Bitmap createBitmapFromView(View view) {
        // Измерьте размер View
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        // Создайте Bitmap и нарисуйте View на нем
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private void enableMyLocationOverlay() {
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        Bitmap iconPerson = BitmapFactory.decodeResource(getResources(), R.drawable.ic_my_tracker_46dp);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(iconPerson, 128, 128, false);
        locationOverlay.setPersonIcon(scaledBitmap);
        locationOverlay.setDirectionIcon(scaledBitmap);
        map.getOverlays().add(locationOverlay);
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationOverlay.disableMyLocation();
        map.onDetach();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}