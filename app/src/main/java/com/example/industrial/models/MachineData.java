package com.example.industrial.models;

import android.os.Parcel;
import android.os.Parcelable;

public class MachineData implements Parcelable {
    private int machine_id;
    private int[] values;
    private String timestamp;

    public MachineData(int machine_id, int[] values, String timestamp) {
        this.machine_id = machine_id;
        this.values = values;
        this.timestamp = timestamp;
    }

    protected MachineData(Parcel in) {
        machine_id = in.readInt();
        values = in.createIntArray();
        timestamp = in.readString();
    }

    public static final Creator<MachineData> CREATOR = new Creator<MachineData>() {
        @Override
        public MachineData createFromParcel(Parcel in) {
            return new MachineData(in);
        }

        @Override
        public MachineData[] newArray(int size) {
            return new MachineData[size];
        }
    };

    public int[] getValues() {
        return values;
    }

    public int getValue(MachineValue value){
        switch(value){
            case Efficiency:
                return getEfficiency();
            case RPM:
                return getSpeed();
            case Temp:
                return getTemp();
            default:
                return 0;
        }
    }

    public int getSpeed(){
        return values[MachineValue.RPM.ordinal()];
    }

    public int getEfficiency(){
        return values[MachineValue.Efficiency.ordinal()];
    }

    public int getTemp(){
        return values[MachineValue.Temp.ordinal()];
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getMachine_id() {
        return machine_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(machine_id);
        dest.writeIntArray(values);
        dest.writeString(timestamp);
    }
}
