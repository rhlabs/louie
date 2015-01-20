/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie.pb;

import java.util.*;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;

import org.joda.time.LocalDate;

import com.rhythm.pb.DataTypeProtos.BoolListPB;
import com.rhythm.pb.DataTypeProtos.BoolPB;
import com.rhythm.pb.DataTypeProtos.BytesListPB;
import com.rhythm.pb.DataTypeProtos.BytesPB;
import com.rhythm.pb.DataTypeProtos.DateListPB;
import com.rhythm.pb.DataTypeProtos.DatePB;
import com.rhythm.pb.DataTypeProtos.DateTimeListPB;
import com.rhythm.pb.DataTypeProtos.DateTimePB;
import com.rhythm.pb.DataTypeProtos.DoubleListPB;
import com.rhythm.pb.DataTypeProtos.DoublePB;
import com.rhythm.pb.DataTypeProtos.FloatListPB;
import com.rhythm.pb.DataTypeProtos.FloatPB;
import com.rhythm.pb.DataTypeProtos.IntListPB;
import com.rhythm.pb.DataTypeProtos.IntPB;
import com.rhythm.pb.DataTypeProtos.LongListPB;
import com.rhythm.pb.DataTypeProtos.LongPB;
import com.rhythm.pb.DataTypeProtos.StringListPB;
import com.rhythm.pb.DataTypeProtos.StringPB;

import com.rhythm.louie.request.data.Data;
import com.rhythm.louie.request.data.DataParser;
import com.rhythm.louie.request.data.PBBuilder;

/**
 * @author cjohnson
 * Created: Jul 29, 2011 5:54:32 PM
 */

public abstract class PBType<T,M extends Message> implements DataParser<T>, PBBuilder<T,M> {
    final String name;
    final Set<String> aliases;
    private PBType(String name,String... aliases) {
        this.name = name;
        if (aliases!=null && aliases.length!=0) {
            HashSet<String> aliasSet = new HashSet<String>(Arrays.asList(aliases));
            this.aliases = Collections.unmodifiableSet(aliasSet);
        } else {
            this.aliases = Collections.emptySet();
        }
    }
    private PBType(String name) {
        this.name = name;
        this.aliases = Collections.emptySet();
    }
    
    private PBType(Descriptor desc) {
        this(desc.getFullName());
    }
    
    private PBType(Descriptor desc,String... aliases) {
        this(desc.getFullName(),aliases);
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public boolean matches(String name) {
        return this.name.equals(name) || aliases.contains(name);
    }
    
    public List<T> parseList(Collection<Data> dataList) throws Exception {
        List<T> values = new ArrayList<T>(dataList.size());
        for (Data data : dataList) {
            values.add(parseData(data));
        }
        return values;
    }
    
    public T toValue(M pb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
/*************************************
******* Type Implementations *********
*************************************/    
    
    public static final PBType<Integer,IntPB> INT = new IntType();
    private static class IntType extends PBType<Integer,IntPB> {
        public IntType() {
            super(IntPB.getDescriptor(),"int");
        }
        
        @Override
        public Integer parseData(Data data) throws Exception {
            IntPB.Builder builder = data.merge(IntPB.newBuilder());
            return builder.getValue();
        }
        
        @Override
        public IntPB build(Integer i) {
            return IntPB.newBuilder().setValue(i).build();
        }
    };
    
    public static final PBType<Collection<Integer>,IntListPB> INT_LIST = new IntListType();
    private static class IntListType extends PBType<Collection<Integer>,IntListPB> {
        public IntListType() {
            super(IntListPB.getDescriptor());
        }
        
        @Override
        public List<Integer> parseData(Data data) throws Exception {
            data.merge(IntListPB.newBuilder()).build();
            IntListPB.Builder builder = data.merge(IntListPB.newBuilder());
            return builder.getValuesList();
        }
        
        @Override
        public IntListPB build(Collection<Integer> list) {
            return IntListPB.newBuilder().addAllValues(list).build();
        }
    };
    
    public static final PBType<Long,LongPB> LONG = new LongType();
    private static class LongType extends PBType<Long,LongPB> {
        public LongType() {
            super(LongPB.getDescriptor(),"long");
        }
        
        @Override
        public Long parseData(Data data) throws Exception {
            LongPB.Builder builder = data.merge(LongPB.newBuilder());
            return builder.getValue();
        }
        
        @Override
        public LongPB build(Long l) {
            return LongPB.newBuilder().setValue(l).build();
        }
    };
    
    public static final PBType<Collection<Long>,LongListPB> LONG_LIST = new LongListType();
    private static class LongListType extends PBType<Collection<Long>,LongListPB> {
        public LongListType() {
            super(LongListPB.getDescriptor());
        }
        
        @Override
        public List<Long> parseData(Data data) throws Exception {
            LongListPB.Builder builder = data.merge(LongListPB.newBuilder());
            return builder.getValuesList();
        }
        
        @Override
        public LongListPB build(Collection<Long> list) {
            return LongListPB.newBuilder().addAllValues(list).build();
        }
    };
    
    public static final PBType<String,StringPB> STRING = new StringType();
    private static class StringType extends PBType<String,StringPB> {
        public StringType() {
            super(StringPB.getDescriptor(),"string");
        }
        
        @Override
        public String parseData(Data data) throws Exception {
            StringPB.Builder builder = data.merge(StringPB.newBuilder());
            return builder.getValue();
        }
        
        @Override
        public StringPB build(String s) {
            return StringPB.newBuilder().setValue(s).build();
        }
    };
    
    public static final PBType<Collection<String>,StringListPB> STRING_LIST = new StringListType();
    private static class StringListType extends PBType<Collection<String>,StringListPB> {
        public StringListType() {
            super(StringListPB.getDescriptor());
        }

        @Override
        public List<String> parseData(Data data) throws Exception {
            StringListPB.Builder builder = data.merge(StringListPB.newBuilder());
            return builder.getValuesList();
        }
        
        @Override
        public StringListPB build(Collection<String> list) {
            return StringListPB.newBuilder().addAllValues(list).build();
        }
   };
    
    public static final PBType<Float,FloatPB> FLOAT = new FloatType();
    private static class FloatType extends PBType<Float,FloatPB> {
        public FloatType() {
            super(FloatPB.getDescriptor(),"float");
        }
        
        @Override
        public Float parseData(Data data) throws Exception {
            FloatPB.Builder builder = data.merge(FloatPB.newBuilder());
            return builder.getValue();
        }
        
        @Override
        public FloatPB build(Float f) {
            return FloatPB.newBuilder().setValue(f).build();
        }
    };
    
    public static final PBType<Collection<Float>,FloatListPB> FLOAT_LIST = new FloatListType();
    private static class FloatListType extends PBType<Collection<Float>,FloatListPB> {
        public FloatListType() {
            super(FloatListPB.getDescriptor());
        }
        
        @Override
        public List<Float> parseData(Data data) throws Exception {
            FloatListPB.Builder builder = data.merge(FloatListPB.newBuilder());
            return builder.getValuesList();
        }
        
        @Override
        public FloatListPB build(Collection<Float> list) {
            return FloatListPB.newBuilder().addAllValues(list).build();
        }
    };
    
    
    public static final PBType<Double,DoublePB> DOUBLE = new DoubleType();
    private static class DoubleType extends PBType<Double,DoublePB> {
        public DoubleType() {
            super(DoublePB.getDescriptor(),"double");
        }
        
        @Override
        public Double parseData(Data data) throws Exception {
            DoublePB.Builder builder = data.merge(DoublePB.newBuilder());
            return builder.getValue();
        }
        
        @Override
        public DoublePB build(Double d) {
            return DoublePB.newBuilder().setValue(d).build();
        }
    };
    
    public static final PBType<Collection<Double>,DoubleListPB> DOUBLE_LIST = new DoubleListType();
    private static class DoubleListType extends PBType<Collection<Double>,DoubleListPB> {
        public DoubleListType() {
            super(DoubleListPB.getDescriptor());
        }
        
        @Override
        public List<Double> parseData(Data data) throws Exception {
            DoubleListPB.Builder builder = data.merge(DoubleListPB.newBuilder());
            return builder.getValuesList();
        }
        
        @Override
        public DoubleListPB build(Collection<Double> list) {
            return DoubleListPB.newBuilder().addAllValues(list).build();
        }
    };
    
    public static final PBType<LocalDate,DatePB> DATE = new DatePBType();
    private static class DatePBType extends PBType<LocalDate,DatePB> {
        public DatePBType() {
            super(DatePB.getDescriptor(),"date");
        }
        
        @Override
        public LocalDate parseData(Data data) throws Exception {
            DatePB.Builder builder = data.merge(DatePB.newBuilder());
            return new LocalDate(builder.getTime());
        }
        
        @Override
        public DatePB build(LocalDate d) {
            return DatePB.newBuilder().setTime(d.toDateTimeAtStartOfDay().getMillis()).build();
        }
        
        @Override
        public LocalDate toValue(DatePB pb) {
            return new LocalDate(pb.getTime());
        }
    };
    
    public static final PBType<Collection<LocalDate>,DateListPB> DATE_LIST = new DateListType();
    private static class DateListType extends PBType<Collection<LocalDate>,DateListPB> {
        public DateListType() {
            super(DateListPB.getDescriptor());
        }
        
        @Override
        public List<LocalDate> parseData(Data data) throws Exception {
            DateListPB.Builder builder = data.merge(DateListPB.newBuilder());
            List<LocalDate> dates = new ArrayList<LocalDate>(builder.getTimesCount());
            for (Long time : builder.getTimesList()) {
                dates.add(new LocalDate(time));
            }
            return dates;
        }
        
        @Override
        public DateListPB build(Collection<LocalDate> list) {
            DateListPB.Builder builder = DateListPB.newBuilder();
            for (LocalDate d : list) {
                builder.addTimes(d.toDateTimeAtStartOfDay().getMillis());
            }
            return builder.build();
        }
        
        @Override
        public Collection<LocalDate> toValue(DateListPB pb) {
            List<LocalDate> dates = new ArrayList<LocalDate>(pb.getTimesCount());
            for (Long time : pb.getTimesList()) {
                dates.add(new LocalDate(time));
            }
            return dates;
        }
    };
    
    public static final PBType<Date,DateTimePB> DATETIME = new DateTimePBType();
    private static class DateTimePBType extends PBType<Date,DateTimePB> {
        public DateTimePBType() {
            super(DateTimePB.getDescriptor(),"datetime");
        }
        
        @Override
        public Date parseData(Data data) throws Exception {
            DateTimePB.Builder builder = data.merge(DateTimePB.newBuilder());
            return new Date(builder.getTime());
        }
        
        @Override
        public DateTimePB build(Date d) {
            return DateTimePB.newBuilder().setTime(d.getTime()).build();
        }
        
        @Override
        public Date toValue(DateTimePB pb) {
            return new Date(pb.getTime());
        }
    };
    
    public static final PBType<Collection<Date>,DateTimeListPB> DATETIME_LIST = new DateTimeListType();
    private static class DateTimeListType extends PBType<Collection<Date>,DateTimeListPB> {
        public DateTimeListType() {
            super(DateTimeListPB.getDescriptor());
        }
        
        @Override
        public List<Date> parseData(Data data) throws Exception {
            DateTimeListPB.Builder builder = data.merge(DateTimeListPB.newBuilder());
            List<Date> dates = new ArrayList<Date>(builder.getTimesCount());
            for (Long time : builder.getTimesList()) {
                dates.add(new Date(time));
            }
            return dates;
        }
        
        @Override
        public DateTimeListPB build(Collection<Date> list) {
            DateTimeListPB.Builder builder = DateTimeListPB.newBuilder();
            for (Date d : list) {
                builder.addTimes(d.getTime());
            }
            return builder.build();
        }
        
        @Override
        public List<Date> toValue(DateTimeListPB pb) {
            List<Date> dates = new ArrayList<Date>(pb.getTimesCount());
            for (Long time : pb.getTimesList()) {
                dates.add(new Date(time));
            }
            return dates;
        }
    };
    
    public static final PBType<Boolean,BoolPB> BOOL = new BoolType();
    public static final BoolPB TRUE = BoolPB.newBuilder().setValue(true).build();
    public static final BoolPB FALSE = BoolPB.newBuilder().setValue(false).build();
    private static class BoolType extends PBType<Boolean,BoolPB> {
        public BoolType() {
            super(BoolPB.getDescriptor(),"boolean","bool");
        }
        
        @Override
        public Boolean parseData(Data data) throws Exception {
            BoolPB.Builder builder = data.merge(BoolPB.newBuilder());
            return builder.getValue();
        }
        
        @Override
        public BoolPB build(Boolean b) {
            if (b) {
                return TRUE;
            } else {
                return FALSE;
            }
        }
    };
    
    public static final PBType<Collection<Boolean>,BoolListPB> BOOL_LIST = new BoolListType();
    private static class BoolListType extends PBType<Collection<Boolean>,BoolListPB> {
        public BoolListType() {
            super(BoolListPB.getDescriptor());
        }
        
        @Override
        public List<Boolean> parseData(Data data) throws Exception {
            BoolListPB.Builder builder = data.merge(BoolListPB.newBuilder());
            return builder.getValuesList();
        }
        
        @Override
        public BoolListPB build(Collection<Boolean> list) {
            return BoolListPB.newBuilder().addAllValues(list).build();
        }
    };
    
    public static final PBType<ByteString,BytesPB> BYTES = new BytesType();
    private static class BytesType extends PBType<ByteString,BytesPB> {
        public BytesType() {
            super(BytesPB.getDescriptor(),"bytes");
        }
        
        @Override
        public ByteString parseData(Data data) throws Exception {
            BytesPB.Builder builder = data.merge(BytesPB.newBuilder());
            return builder.getValue();
        }
        
        @Override
        public BytesPB build(ByteString b) {
            return BytesPB.newBuilder().setValue(b).build();
        }
    };
    
    public static final PBType<Collection<ByteString>,BytesListPB> BYTES_LIST = new BytesListType();
    private static class BytesListType extends PBType<Collection<ByteString>,BytesListPB> {
        public BytesListType() {
            super(BytesListPB.getDescriptor());
        }
        
        @Override
        public List<ByteString> parseData(Data data) throws Exception {
            BytesListPB.Builder builder = data.merge(BytesListPB.newBuilder());
            return builder.getValuesList();
        }
        
        @Override
        public BytesListPB build(Collection<ByteString> list) {
            return BytesListPB.newBuilder().addAllValues(list).build();
        }
    };
}
