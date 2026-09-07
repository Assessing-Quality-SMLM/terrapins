package com.coxphysics.terrapins.vendored.thunderstorm.ImportExport;

import com.coxphysics.terrapins.vendored.thunderstorm.IModule;
import com.coxphysics.terrapins.vendored.thunderstorm.results.GenericTable;
import java.io.IOException;
import java.util.List;

public interface IImportExport extends IModule {
    
    public String getName();
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws IOException;
    public void exportToFile(String fp, int floatPrecision, GenericTable table, List<String> columns) throws IOException;
    public String getSuffix();  // filename suffix
   
}
