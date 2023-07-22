package com.example.map.GoogleMapWork;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class PlaceModel implements Parcelable {

    public String id;

    public String name;
    public String latitude;
    public String longitude;

    public String city;
    public String state;


    public PlaceModel() {
    }

    protected PlaceModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        city = in.readString();
        state = in.readString();
    }

    public static final Creator<PlaceModel> CREATOR = new Creator<PlaceModel>() {
        @Override
        public PlaceModel createFromParcel(Parcel in) {
            return new PlaceModel(in);
        }

        @Override
        public PlaceModel[] newArray(int size) {
            return new PlaceModel[size];
        }
    };

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(latitude);
        parcel.writeString(longitude);
        parcel.writeString(city);
        parcel.writeString(state);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
