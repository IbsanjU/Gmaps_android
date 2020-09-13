package com.ibsanju.gmaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnPolygonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public
class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnInfoWindowCloseListener,
        OnMarkerClickListener,
        OnMapClickListener,
        OnMapLongClickListener,
        OnPolygonClickListener,
        GoogleMap.OnPolylineClickListener {
    private static final String TAG = "MapsActivity";
    private static final int PATTERN_GAP_LENGTH_PX = 15;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    public Polyline polyline;
    public Polygon polygon;
    int tag = 0;
    private String[] mtag = {"A", "B", "C", "D"};
    private double[] dist = {0, 0, 0, 0};
    private LatLng tempLat;
    private int position;
    private GoogleMap googleMap;
    private ArrayList<LatLng> latLngs;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private UiSettings uiSetting;
    private Marker mSelectedMarker;
    private ArrayList<Polyline> lines;

    public
    void centerMapOnLocation(Location location, String title) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        latLngs = new ArrayList<>();
        lines   = new ArrayList<>();

        locationManager  = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public
            void onLocationChanged(Location location) {
                centerMapOnLocation(location, "Your Location");
            }

            @Override
            public
            void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public
            void onProviderEnabled(String provider) {
            }

            @Override
            public
            void onProviderDisabled(String provider) {
            }
        };
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public
    void onMapReady(GoogleMap map) {
        googleMap = map;

        uiSetting = googleMap.getUiSettings();
        uiSetting.setMyLocationButtonEnabled(true);
        //        googleMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng zoom = new LatLng(40, -90);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoom, 3));


        // Set listeners for click events.
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnPolygonClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMarkerClickListener(this);
    }

    private
    void markQuadrilateral() {
        if (latLngs.size() == 4) {
//            polyline.remove();
//            polygon.remove();
            Log.i(TAG, "markQuadrilateral: updating: " + latLngs);
            polygon = googleMap.addPolygon(new PolygonOptions()
                                                   .clickable(true)
                                                   .fillColor(3)
                                                   .add(latLngs.get(0), latLngs.get(1), latLngs.get(2), latLngs.get(3))
            );
            stylePolygon(polygon);

            for (int l = 0; l < latLngs.size() - 1; l++) {
                dist[l] = distance(latLngs.get(l), latLngs.get(l + 1));
            }
            dist[latLngs.size() - 1] = distance(latLngs.get(0), latLngs.get(3));
            markPolylines(latLngs.get(0), latLngs.get(1), "A");
            markPolylines(latLngs.get(1), latLngs.get(2), "B");
            markPolylines(latLngs.get(2), latLngs.get(3), "C");
            markPolylines(latLngs.get(0), latLngs.get(3), "D");
        }
    }

    public
    void markPolylines(LatLng prelat, LatLng curlat, String t) {
        polyline = googleMap.addPolyline(new PolylineOptions()
                                                 .clickable(true)
                                                 .add(prelat, curlat));
        polyline.setTag(t);
        lines.add(polyline);
        stylePolyline(this.polyline);
    }


    private
    void stylePolyline(Polyline polyline) {
//        polyline.setEndCap(new RoundCap());
        polyline.setWidth(10);
        polyline.setColor(Color.RED);
        polyline.setJointType(JointType.DEFAULT);
    }

    public
    LatLng midPoint(double lat1, double lon1, double lat2, double lon2) {
       double lat3 = (lat1+lat2)/2;
        double lon3 = (lon1+lon2)/2;
        return new LatLng(lat3, lon3);
    }

    @Override
    public
    void onPolylineClick(Polyline polyline) {
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            polyline.setPattern(null);
        }


        if ("A".equals(polyline.getTag().toString())) {
//            customToast("distance between A and B: " + distance(latLngs.get(0), latLngs.get(1)) + " Km");
            markMidPoint(midPoint(latLngs.get(0).latitude,
                                  latLngs.get(0).longitude,
                                  latLngs.get(1).latitude,
                                  latLngs.get(1).longitude), "distance between A and B: " + distance(latLngs.get(0), latLngs.get(1)) + " Km");
        } else if ("B".equals(polyline.getTag().toString())) {
            markMidPoint(midPoint(latLngs.get(1).latitude,
                                  latLngs.get(1).longitude,
                                  latLngs.get(2).latitude,
                                  latLngs.get(2).longitude), "distance between B and C: " + distance(latLngs.get(1), latLngs.get(2)) + " Km");
        } else if ("C".equals(polyline.getTag().toString())) {
            markMidPoint(midPoint(latLngs.get(2).latitude,
                                  latLngs.get(2).longitude,
                                  latLngs.get(3).latitude,
                                  latLngs.get(3).longitude), "distance between C and D: " + distance(latLngs.get(2), latLngs.get(3)) + " Km");
        } else if ("D".equals(polyline.getTag().toString())) {
            markMidPoint(midPoint(latLngs.get(3).latitude,
                                  latLngs.get(3).longitude,
                                  latLngs.get(0).latitude,
                                  latLngs.get(0).longitude), "distance between D and A: " + distance(latLngs.get(3), latLngs.get(0)) + " Km");
        }

//        customToast("Route type " + polyline.getTag().toString());
    }

    private
    void markMidPoint(LatLng latLng, String text) {

        BitmapDescriptor transparent = BitmapDescriptorFactory.fromResource(R.drawable.scale);
        Marker m = this.googleMap.addMarker(new MarkerOptions()
                                               .position(latLng)
                                               .title(text)
                                               .icon(transparent)
//                                               .anchor(0f, 0.5f)
                                               .visible(true)
                                               .draggable(false));
        m.showInfoWindow();

    }

    @Override
    public
    void onPolygonClick(Polygon polygon) {
        double distance = totalDistance();
        customToast("Polygon Distance = " + distance);
    }

    private
    void stylePolygon(Polygon polygon) {
        polygon.setStrokePattern(null);
        polygon.setStrokeWidth(8);
        polygon.setStrokeColor(Color.TRANSPARENT);
        polygon.setFillColor(0x5900FF00);
    }

    private
    double distance(LatLng mlat1, LatLng mlat2) {
        double lon1  = mlat1.longitude;
        double lon2  = mlat2.longitude;
        double lat1  = mlat1.latitude;
        double lat2  = mlat2.latitude;
        double theta = lon1 - lon2;
        double temp = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        temp = Math.acos(temp);
        temp = rad2deg(temp);
        temp = temp * 60 * 1.1515;
        temp = Math.round(temp * 100.0) / 100.0;
        Log.i(TAG, "distance: " + temp);
        return temp;
    }

    private
    double totalDistance() {
        double sum = 0;
        for (int i = 0; i < dist.length; i++) {
            sum = +dist[i];
            return sum;
        }
        return sum;
    }

    private
    double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private
    double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public
    void onMapClick(LatLng latLng) {
    }

    @Override
    public
    void onMapLongClick(LatLng latLng) {
        if (latLngs.size() < 4) {
            latLngs.add(latLng);
            LatLng mlat = latLng;
            markLocation(latLng);
//            if (latLngs.size() > 1) {
//                markPolylines(latLngs.get(latLngs.size() - 2), latLng, mtag[tag - 1]);
//            }
            tag += 1;
        } else {
            googleMap.clear();
            tag = 0;
            latLngs.clear();
        }
    }

    private
    void markLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Log.i(TAG, "onMapLongClick: " + listAddresses.get(0).toString());

            if (listAddresses != null && listAddresses.size() > 0) {
                String address = "";
                String title   = "";
                String snippet = "";


                if (listAddresses.get(0).getThoroughfare() != null) {
                    address += listAddresses.get(0).getThoroughfare() + " ";
                    title += listAddresses.get(0).getThoroughfare() + " ";
                }
                if (listAddresses.get(0).getSubThoroughfare() != null) {
                    address += listAddresses.get(0).getSubThoroughfare() + " ";
                    title += listAddresses.get(0).getSubThoroughfare() + " ";
                }

                if (listAddresses.get(0).getLocality() != null) {
                    address += listAddresses.get(0).getLocality() + " ";
                    snippet += listAddresses.get(0).getLocality() + " ";
                }

                if (listAddresses.get(0).getPostalCode() != null) {
                    address += listAddresses.get(0).getPostalCode() + " ";
                    title += listAddresses.get(0).getPostalCode();
                }

                if (listAddresses.get(0).getAdminArea() != null) {
                    address += listAddresses.get(0).getAdminArea();
                    snippet += listAddresses.get(0).getAdminArea();
                }

//                customToast(address);
                Log.i("Address", address);

//                =======================================================================

                Bitmap.Config           conf    = Bitmap.Config.ARGB_8888;
                Bitmap                  bmp     = Bitmap.createBitmap(130, 100, conf);
                android.graphics.Canvas canvas1 = new Canvas(bmp);

// paint defines the text color, stroke width and size
                Paint color = new Paint();
                color.setTextSize(40);
                color.setColor(Color.BLACK);

// modify canvas
                canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.round_pin_drop_black), -15, -6, color);
                canvas1.drawText(mtag[tag], 45, 40, color);

// add marker to Map
//                =======================================================================

                Marker m1 = googleMap.addMarker(new MarkerOptions()
                                                        .position(latLng)
                                                        .title(title)
                                                        .snippet(snippet)
//                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.round_pin_drop_black))
                                                        .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                                                        .anchor(0f, 0.5f)
                                                        .visible(true)
                                                        .draggable(true));
                m1.showInfoWindow();
                m1.setTag(mtag[tag]);
                markQuadrilateral();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public
    boolean onMarkerClick(Marker marker) {
        Log.i(TAG, "onMarkerClick: markerPosition " + marker.getPosition());
//        customToast("TAG: " + marker.getTag().toString());

        if (marker.equals(mSelectedMarker)) {
            mSelectedMarker = null;
            tag             = 0;
            latLngs.clear();
            googleMap.clear();
            return true;
        }
        mSelectedMarker = marker;
        return false;
    }

    @Override
    public
    void onInfoWindowClick(Marker marker) {

    }

    @Override
    public
    void onInfoWindowClose(Marker marker) {

    }

    @Override
    public
    void onMarkerDragStart(Marker marker) {
    }

    @Override
    public
    void onMarkerDrag(Marker marker) {

    }

    @Override
    public
    void onMarkerDragEnd(Marker marker) {
        if (this.latLngs.size() > 3) {
            this.polygon.remove();
            this.polyline.remove();
            Iterator<Polyline> iter = lines.iterator();
            while (iter.hasNext()) {
                Polyline p = iter.next();
                p.remove();
                iter.remove();
            }
        }
        Log.i(TAG, "onMarkerDragEnd: Position: " + marker.getPosition());
//        customToast("Tag:" + marker.getTag().toString());
        try {
            if ("A".equals(marker.getTag().toString())) {
                position = 0;
            } else if ("B".equals(marker.getTag().toString())) {
                position = 1;
            } else if ("C".equals(marker.getTag().toString())) {
                position = 2;
            } else if ("D".equals(marker.getTag().toString())) {
                position = 3;
            }
            latLngs.set(position, new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));

        } catch (Exception e) {
            e.printStackTrace();
        }
        latLngs.set(position, new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
        Log.i(TAG, "markQuadrilateral: position: " + position + " Updated = " + latLngs.get(position));

        markQuadrilateral();
    }

    private
    void customToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
//        Snackbar.make(View, s, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }


}

















