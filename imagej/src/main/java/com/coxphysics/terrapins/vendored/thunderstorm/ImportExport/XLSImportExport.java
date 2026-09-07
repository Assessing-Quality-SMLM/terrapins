package com.coxphysics.terrapins.vendored.thunderstorm.ImportExport;

public class XLSImportExport extends DLMImportExport implements IImportExport {

    public XLSImportExport() {
        super("\t");
    }
    
    @Override
    public String getName() {
        return "XLS (tab separated)";
    }

    @Override
    public String getSuffix() {
        return "xls";
    }

}
