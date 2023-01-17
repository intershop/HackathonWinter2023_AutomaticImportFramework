package com.zamro.adapter.ac_search_solr_ext.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.intershop.adapter.search_solr.internal.AbstractSuggestResult;
import com.intershop.adapter.search_solr.internal.SolrSuggestResultItem;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.xcs.capi.catalog.CatalogCategory;
import com.intershop.beehive.xcs.capi.catalog.CatalogMgr;
import com.intershop.component.mvc.capi.catalog.Catalog;
import com.intershop.component.mvc.capi.catalog.MVCatalogMgr;
import com.intershop.component.search.capi.SearchResult;
import com.intershop.component.search.capi.SuggestResultItem;

public class ZMRSuggestResult extends AbstractSuggestResult implements SearchResult
{
    
    private CatalogMgr catalogMgr;
    
    private MVCatalogMgr mvCatalogMgr;

    private final String fieldName;
    
    private final QueryResponse solrQueryResponse;
    
    Comparator<SuggestResultItem> compareSuggestResultItem = new Comparator<SuggestResultItem>() {
        @Override
        public int compare(SuggestResultItem o1, SuggestResultItem o2) {
            return o1.getHitCount() < o2.getHitCount() ? -1 : o1.getHitCount() > o2.getHitCount() ? 1 : 0;
        }
    };
    
    /*
    * @param fieldName
    *            the name of the field that was queried
    * 
    * @param solrQueryResponse
    *            the response returned by SOLR engine to the search request, or
    *            <code>null</code>
    */
   public ZMRSuggestResult(String fieldName, QueryResponse solrQueryResponse){
       this.fieldName = fieldName;
       this.solrQueryResponse = solrQueryResponse;
       this.catalogMgr = NamingMgr.getManager(CatalogMgr.class);
       this.mvCatalogMgr = NamingMgr.getManager(MVCatalogMgr.class);       
   }    

   @Override
   public Iterator<SuggestResultItem> getHits()
   {
       Set<SuggestResultItem> hits = new HashSet<SuggestResultItem>();

       if (solrQueryResponse != null)
       {
           SolrDocumentList documents = solrQueryResponse.getResults();
           
           HashMap<String, Integer> catIds = new HashMap<String, Integer>();
           
           if (documents != null){
               for (SolrDocument document : documents){
                   // get the full category path information
                   Collection<Object> multi = document.getFieldValues("CategoryUUIDLevelMulti");
                   // after '=' we find the category UUID   
                   for (Object LevelX : multi){
                       String catId = null;
                       String [] parts = ((String)LevelX).split("=");
                       if (parts != null && parts.length > 1){
                           catId = parts[parts.length -1];
                           if(catIds.containsKey(catId)){
                               catIds.put(catId, new Integer(catIds.get(catId).intValue() + 1));
                           }else{
                               catIds.put(catId, new Integer(1));
                           }
                       }                               
                   }                                           
               }
           }

           // for all categories we have found
           for(String catId : catIds.keySet()){
               CatalogCategory category = catalogMgr.resolveCatalogCategoryFromID(catId);
               Catalog catalog = mvCatalogMgr.getCatalogByCatalogDomain(category.getDomain()); 
               String  result = catalog.getId() + "|" + category.getName() + "|" + category.getDisplayName();                            
               hits.add(new SolrSuggestResultItem(result, catIds.get(catId), "CategoryInfo"));                               
           }
       }
       return hits.iterator();
   }
   
   public Iterator<SuggestResultItem> getHitsCat(){
       Set<SuggestResultItem> hits = new TreeSet<SuggestResultItem>(compareSuggestResultItem.reversed());
       Map<String, Integer> mapCategories = new HashMap<String, Integer>();
       
       if (solrQueryResponse != null)
       {
           
           FacetField manufacturerNameFacet = solrQueryResponse.getFacetField("CategoryUUIDLevelMulti");
           
           if(manufacturerNameFacet != null){
               for(Count c: manufacturerNameFacet.getValues()){
                   String cat = c.getName().split("=")[c.getName().split("=").length-1];
                   while(mapCategories.containsValue((int) c.getCount())){
                       c.setCount(c.getCount()+1);
                   }
                   mapCategories.put(cat, (int) c.getCount());
               }
           }
           
       }
       Map<String, Integer> sortedMap = sortByValue(mapCategories);
       
       for(Entry<String, Integer> c : sortedMap.entrySet()){
           CatalogCategory category = catalogMgr.resolveCatalogCategoryFromID(c.getKey());
           Catalog catalog = mvCatalogMgr.getCatalogByCatalogDomain(category.getDomain()); 
           String  result = catalog.getId() + "|" + category.getName() + "|" + category.getDisplayName();                            
           hits.add(new SolrSuggestResultItem(result, c.getValue().intValue(), "CategoryInfo"));
       }
       
       return hits.iterator();
   }
   

   public Iterator<SuggestResultItem> getHitsBrands(){
       Set<SuggestResultItem> hits = new TreeSet<SuggestResultItem>(compareSuggestResultItem.reversed());
       Map<String, Integer> mapBrands = new HashMap<String, Integer>();
       
       if (solrQueryResponse != null)
       {
           
           FacetField manufacturerNameFacet = solrQueryResponse.getFacetField("manufacturerName");
           
           if(manufacturerNameFacet != null){
               for(Count c: manufacturerNameFacet.getValues()){
                   while(mapBrands.containsValue((int) c.getCount())){
                       c.setCount(c.getCount()+1);
                   }
                   mapBrands.put(c.getName(), (int) c.getCount());
               }
           }
           
       }
       Map<String, Integer> sortedMap = sortByValue(mapBrands);
       
       for(Entry<String, Integer> c : sortedMap.entrySet()){
           hits.add(new SolrSuggestResultItem(c.getKey(), c.getValue().intValue() , "ManufacturerName"));
       }
       
       return hits.iterator();
   }
   
   public Iterator<SuggestResultItem> getHitsTerms(){
       Set<SuggestResultItem> hits = new TreeSet<SuggestResultItem>(compareSuggestResultItem.reversed());
       Map<String, Integer> mapTerms = new HashMap<String, Integer>();
       
       if (solrQueryResponse != null)
       {
           
           FacetField suggestTermsFacet = solrQueryResponse.getFacetField("_spell");
           
           if(suggestTermsFacet != null){
               for(Count c: suggestTermsFacet.getValues()){
                   while(mapTerms.containsValue((int) c.getCount())){
                       c.setCount(c.getCount()+1);
                   }
                   mapTerms.put(c.getName(), (int) c.getCount());
               }
           }
           
       }
       Map<String, Integer> sortedMap = sortByValue(mapTerms);
       
       for(Entry<String, Integer> c : sortedMap.entrySet()){
           hits.add(new SolrSuggestResultItem(c.getKey(), c.getValue().intValue() , "SuggestValue"));
       }
       
       return hits.iterator();
   }
   
   public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

       List<Map.Entry<K, V>> list =
               new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

       Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
           @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
               return (o1.getValue()).compareTo(o2.getValue());
           }
       });

       Map<K, V> result = new LinkedHashMap<K, V>();
       for (Map.Entry<K, V> entry : list) {
           result.put(entry.getKey(), entry.getValue());
       }

       return result;

   }
   
}
