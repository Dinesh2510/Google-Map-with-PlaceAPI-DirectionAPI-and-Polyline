package com.example.map.GoogleMapWork;


import android.os.Parcel;
import android.os.Parcelable;

public class PlaceModel implements Parcelable {

    public String id;
    public String bde_id;
    public String route_id;

    public PlaceModel() {
    }

    protected PlaceModel(Parcel in) {
        id = in.readString();
        bde_id = in.readString();
        route_id = in.readString();
        name = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        branchAssigned = in.readString();
        contactPerson = in.readString();
        mobile = in.readString();
        phone = in.readString();
        email = in.readString();
        address = in.readString();
        city = in.readString();
        state = in.readString();
        category = in.readString();
        photo = in.readString();
        createdDate = in.readString();
        updatedDate = in.readString();
        isDeleted = in.readString();
        sequence_id = in.readString();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBde_id() {
        return bde_id;
    }

    public void setBde_id(String bde_id) {
        this.bde_id = bde_id;
    }

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getName() {
        return name;
    }

    public String getSequence_id() {
        return sequence_id;
    }

    public void setSequence_id(String sequence_id) {
        this.sequence_id = sequence_id;
    }

    public String getPhotoCreatedDate() {
        return photoCreatedDate;
    }

    public void setPhotoCreatedDate(String photoCreatedDate) {
        this.photoCreatedDate = photoCreatedDate;
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

    public String getBranchAssigned() {
        return branchAssigned;
    }

    public void setBranchAssigned(String branchAssigned) {
        this.branchAssigned = branchAssigned;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String name;
    public String latitude;
    public String longitude;
    public String branchAssigned;
    public String contactPerson;
    public String mobile;
    public String phone;
    public String email;
    public String address;
    public String city;
    public String state;
    public String category;
    public String photo;
    public String createdDate;
    public String updatedDate;
    public String sequence_id;
    public String isDeleted;
    public String photoCreatedDate;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(bde_id);
        dest.writeString(route_id);
        dest.writeString(name);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(branchAssigned);
        dest.writeString(contactPerson);
        dest.writeString(mobile);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(category);
        dest.writeString(photo);
        dest.writeString(createdDate);
        dest.writeString(updatedDate);
        dest.writeString(isDeleted);
        dest.writeString(sequence_id);
    }
}
