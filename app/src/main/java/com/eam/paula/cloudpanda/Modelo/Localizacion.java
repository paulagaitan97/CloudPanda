package com.eam.paula.cloudpanda.Modelo;

import android.os.Parcel;
import android.os.Parcelable;

public class Localizacion implements Parcelable {

    private String id;
    private double latitude;
    private double longitude;
    private String date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    public Localizacion() {
    }

    protected Localizacion(Parcel in) {
        this.id = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        long tmpDate = in.readLong();
    }

    public static final Creator<Localizacion> CREATOR = new Creator<Localizacion>() {
        @Override
        public Localizacion createFromParcel(Parcel source) {
            return new Localizacion(source);
        }

        @Override
        public Localizacion[] newArray(int size) {
            return new Localizacion[size];
        }
    };

    @Override
    public String toString() {
        return "LocationModel{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", date=" + date +
                '}';
    }
}
