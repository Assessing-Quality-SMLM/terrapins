package com.coxphysics.terrapins.vendored.thunderstorm.ImportExport;

public class CSVImportExport extends DLMImportExport implements IImportExport {

    public CSVImportExport() {
        super(",");
    }

    @Override
    public String getName() {
        return "CSV (comma separated)";
    }

    @Override
    public String getSuffix() {
        return "csv";
    }

}
