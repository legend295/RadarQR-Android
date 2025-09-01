package com.radarqr.dating.android.utility.instatag;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class TaggedUser implements Parcelable {

    @SerializedName("id")
    private String id;
    private String _id;
    @SerializedName("name")
    private String name;
    @SerializedName("x_coordinate")
    private float x_coordinate;
    @SerializedName("y_coordinate")
    private float y_coordinate;

    public TaggedUser(String id, String name, float x_co_ord, float y_co_ord) {
        this.id = id;
        this.name = name;
        this.x_coordinate = x_co_ord;
        this.y_coordinate = y_co_ord;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getX_coordinate() {
        return x_coordinate;
    }

    public void setX_coordinate(float x_coordinate) {
        this.x_coordinate = x_coordinate;
    }

    public float getY_coordinate() {
        return y_coordinate;
    }

    public void setY_coordinate(float y_coordinate) {
        this.y_coordinate = y_coordinate;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeValue(this.x_coordinate);
        dest.writeValue(this.y_coordinate);
    }

    private TaggedUser(Parcel in) {
        this.name = in.readString();
        this.x_coordinate = (float) in.readValue(float.class.getClassLoader());
        this.y_coordinate = (float) in.readValue(float.class.getClassLoader());
    }

    public static final Creator<TaggedUser> CREATOR =
            new Creator<TaggedUser>() {
                @Override
                public TaggedUser createFromParcel(Parcel source) {
                    return new TaggedUser(source);
                }

                @Override
                public TaggedUser[] newArray(int size) {
                    return new TaggedUser[size];
                }
            };
}
