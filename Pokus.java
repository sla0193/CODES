/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vsb.gis.ruz76.geotools;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author Vendula
 */
public class Pokus {

    private SimpleFeatureIterator getFeatures(String path) throws IOException {
        FileDataStore store = FileDataStoreFinder.getDataStore(new File(path));
        SimpleFeatureSource fs = store.getFeatureSource();

        SimpleFeatureIterator sfi = fs.getFeatures().features();
        return sfi;
    }

    public List<SimpleFeature> getListOfFeatures(String path) throws Exception {
        FileDataStore store = FileDataStoreFinder.getDataStore(new File(path));
        SimpleFeatureSource fs = store.getFeatureSource();

        SimpleFeatureIterator states_sfi = fs.getFeatures().features();

        List<SimpleFeature> features = new ArrayList<>();
        while (states_sfi.hasNext()) {
            SimpleFeature feature = states_sfi.next();
            features.add(feature);
        }
        return features;
    }

    public void showAtr(String path) throws Exception {
        List<SimpleFeature> features = new ArrayList<>();
        features = this.getListOfFeatures(path);
        for (SimpleFeature feature : features) {
            System.out.println(feature.getAttributes());
        }

    }

    public void newPointShp(String path, String newPath) throws Exception {
        final SimpleFeatureType TYPE = DataUtilities.createType("Location", "the_geom:Point," + "UMISTENI:Integer," + "REDUKCE:Integer," + "ZOBR_VYSKA:Double");
        System.out.println("TYPE:" + TYPE);
        List<SimpleFeature> listOfPoints = this.getListOfFeatures(path);
        List<SimpleFeature> newList = new ArrayList<>();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

        for (SimpleFeature feature : listOfPoints) {

            Point point = (Point) feature.getDefaultGeometry();
            featureBuilder.add(point);
            featureBuilder.add(feature.getAttribute(1));
            featureBuilder.add(feature.getAttribute(2));
            featureBuilder.add(feature.getAttribute(3));
            
            SimpleFeature ft = featureBuilder.buildFeature(null);
            newList.add(ft);
        }

        File newFile = new File(newPath);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        HashMap<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        newDataStore.createSchema(TYPE);

        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

        System.out.println("SHAPE:" + SHAPE_TYPE);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, newList);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
            //System.exit(0); // success!
        } else {
            System.out.println(typeName + " does not support read/write access");
            //System.exit(1);
        }
    }
    
    

    public void lineToPoint() throws Exception {
        final SimpleFeatureType TYPE = DataUtilities.createType("Location", "the_geom:Point," + "name:String," + "number:Integer");
        System.out.println("TYPE:" + TYPE);
        List<SimpleFeature> features = new ArrayList<>();

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        /* Longitude (= x coord) first ! */
//        Point point = geometryFactory.createPoint(new Coordinate(-468768.21732441336,-468768.21732441336));
//
//        featureBuilder.add(point);
//        featureBuilder.add("cesta");
//        featureBuilder.add(1);
//        SimpleFeature feature = featureBuilder.buildFeature(null);
//        features.add(feature);
        String path = "data\\Silnice.shp";
        FileDataStore store2 = FileDataStoreFinder.getDataStore(new File(path));
        SimpleFeatureSource featureSource2 = store2.getFeatureSource();

        SimpleFeatureIterator states_sfi = featureSource2.getFeatures().features();
        while (states_sfi.hasNext()) {
            SimpleFeature state = states_sfi.next();
            System.out.println(state.getAttribute(1));
            MultiLineString mls = (MultiLineString) state.getDefaultGeometry();
            for (int i = 0; i < mls.getCoordinates().length; i++) {
                double x = mls.getCoordinates()[i].x;
                double y = mls.getCoordinates()[i].y;
                Point point = geometryFactory.createPoint(new Coordinate(x, y));

                featureBuilder.add(point);
                featureBuilder.add("cesta");
                featureBuilder.add(1);
                SimpleFeature feature = featureBuilder.buildFeature(null);
                features.add(feature);
            }
        }

        File newFile = new File("data\\novyShp.shp");
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        HashMap<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        /*
         * TYPE is used as a template to describe the file contents
         */
        newDataStore.createSchema(TYPE);
        /*
         * Write the features to the shapefile
         */
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
        /*
         * The Shapefile format has a couple limitations:
         * - "the_geom" is always first, and used for the geometry attribute name
         * - "the_geom" must be of type Point, MultiPoint, MuiltiLineString, MultiPolygon
         * - Attribute names are limited in length 
         * - Not all data types are supported (example Timestamp represented as Date)
         * 
         * Each data store has different limitations so check the resulting SimpleFeatureType.
         */
        System.out.println("SHAPE:" + SHAPE_TYPE);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            /*
             * SimpleFeatureStore has a method to add features from a
             * SimpleFeatureCollection object, so we use the ListFeatureCollection
             * class to wrap our list of features.
             */
            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
            //System.exit(0); // success!
        } else {
            System.out.println(typeName + " does not support read/write access");
            //System.exit(1);
        }
    }
}
