define(['ProtoBuf'], function(ProtoBuf) {
    
        //this should be restructured. needs to abstract the parsers so they're immutable props, and also needs to lazy load the parsers as required. then once loaded they're shared as a singleton???
        
        var dataProto = ProtoBuf.loadProtoFile("proto/louie/datatype.proto");
        this.ParamPB = dataProto.build("louie.ParamPB"),
        this.MapPB = dataProto.build("louie.MapPB"),
        this.DictionaryPB = dataProto.build("louie.DictionaryPB"),
        this.DatePB = dataProto.build("louie.DatePB"),
        this.DateListPB = dataProto.build("louie.DateListPB"),
        this.DateTimePB = dataProto.build("louie.DateTimePB");
        this.DateTimeListPB = dataProto.build("louie.DateTimeListPB");
        this.BoolPB = dataProto.build("louie.BoolPB");
        this.BoolListPB = dataProto.build("louie.BoolListPB");
        this.IntPB = dataProto.build("louie.IntPB");
        this.IntListPB = dataProto.build("louie.IntListPB");
        this.UIntPB = dataProto.build("louie.UIntPB");
        this.UIntListPB = dataProto.build("louie.UIntListPB");
        this.LongPB = dataProto.build("louie.LongPB");
        this.LongListPB = dataProto.build("louie.LongListPB");
        this.StringPB = dataProto.build("louie.StringPB");
        this.StringListPB = dataProto.build("louie.StringListPB");
        this.FloatPB = dataProto.build("louie.FloatPB");
        this.FloatListPB = dataProto.build("louie.FloatListPB");
        this.DoublePB = dataProto.build("louie.DoublePB");
        this.DoubleListPB = dataProto.build("louie.DoubleListPB");
        this.BytesPB = dataProto.build("louie.BytesPB");
        this.BytesListPB = dataProto.build("louie.BytesListPB");
        console.log("datatypebuilder executed");    
        //all properties are not actually encapsulated or protected.
        return this;

});