package com.zamro.adapter.ac_search_solr_ext.internal;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intershop.adapter.search_solr.internal.SolrIndexExtension;
import com.intershop.adapter.search_solr.internal.SolrSearchResultImpl;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.component.search.capi.SearchIndexException;
import com.intershop.component.search.capi.SearchIndexFeature;
import com.intershop.component.search.capi.SearchIndexQuery;
import com.intershop.component.search.capi.SearchResult;
import com.intershop.component.search.internal.SearchIndexFeatureImpl;

public class ZMRSolrIndexExtension extends SolrIndexExtension
{

    @Inject
    public ZMRSolrIndexExtension(@Assisted Domain domain, @Assisted String indexID)
    {
        super(domain, indexID);
        SearchIndexFeature sif = this.getSearchIndexFeature();
        
        if (sif instanceof SearchIndexFeatureImpl)
        {
            ((SearchIndexFeatureImpl)sif).setObjectsResultClass("com.zamro.adapter.ac_search_solr_ext.internal.ZMRResolveProduct");
        }
        
    }

    
    @Override
    public SearchResult query(SearchIndexQuery query) throws SearchIndexException {
        SolrQuery solrQuery = createSolrQuery(query);
        /*
         * return all fields
         */
        solrQuery.add("fl", new String[] { "*,score" });
        try
        {
            QueryResponse queryResponse = getSolrServer().query(solrQuery, SolrRequest.METHOD.POST);
          
            return new SolrSearchResultImpl(query, queryResponse, this, false);
        }
        catch (SolrServerException e)
        {
            Logger.error(this, "Error during Solr server communication.", e);
        }
        
        return null;
        
    }
    
    @Override
    public SearchResult getSuggestResult(SearchIndexQuery searchIndexQuery)
    {
        // avoid ":" in search query term, QC 159
        String queryTerm = searchIndexQuery.getQueryTerm();
        if (queryTerm.contains(":"))
        {
            searchIndexQuery.setQueryTerm(queryTerm.replaceAll("\\:", ""));
        }

        String suggestField = null;
        
        if (searchIndexQuery.getQueryAttribute() != null && getConfiguration().getAttributeByName(searchIndexQuery.getQueryAttribute()) != null)
        {
            //if explicit field suggest use the standard facet based suggest
            return super.getSuggestResult(searchIndexQuery); 
        }
        else
        {
            //default _suggest field 
            suggestField = "_suggest";
            
            //use only product name if available
            /*
            if(getConfiguration().getAttributeByName("name") != null)
            {
                suggestField = "name"; 
            }
            */
        }
        
        SolrQuery query = createSolrSuggestQuery(searchIndexQuery, suggestField);
        
        QueryResponse response = null;

        try
        {
            response = getSolrServer().query(query, METHOD.POST);
        }
        catch(SolrServerException ex)
        {
            //Logger.error(this, ERROR_MSG_SOLR_SERVER_COMMUNICATION, ex);
        }

        SearchResult suggestResult = new ZMRSuggestResult(suggestField, response);
        return suggestResult;    
    }
    

    @Override
    protected SolrQuery createSolrSuggestQuery(SearchIndexQuery searchIndexQuery, String solrSuggestField)
    {
        
        String queryTerm = searchIndexQuery.getQueryTerm();
        if (queryTerm == null || "".equals(queryTerm))
        {
            return null;
        }
        
        SolrQuery solrQuery = new SolrQuery();
        StringBuilder queryTermBuilder = new StringBuilder();  

        // include all document fields into the result
        solrQuery.setFields("");
        solrQuery.add(CommonParams.QT, "standard");
        solrQuery.add("q.op", "OR");                
        solrQuery.add("rows", "0");
        
        queryTerm = queryTerm.toLowerCase();
        //String[] queryTerms = queryTerm.split("\\P{L}+");
        String[] queryTerms = queryTerm.trim().split("\\s+");
        boolean firstTerm = true;
        for (String term : queryTerms)
        {
            
            if (!firstTerm && term.length() > 0)//add white space between different terms
            {
                queryTermBuilder.append(' ');
            }
            if (firstTerm)
            {
                firstTerm = false;
            }
            
            if (term.length() > 0)//add query term for non-empty terms
            {
                queryTermBuilder.append(solrSuggestField).append(":").append(term).append(' ');
                queryTermBuilder.append("name").append(":").append(term);
            }

        }          
        
        // do not add variations (isMastered=0): 
        // queryTermBuilder.append(' ').append("isMastered").append(":").append("0");
  
        if (queryTermBuilder.length() == 0)
        {
            queryTermBuilder.append(solrSuggestField).append(":").append("/");// if all terms were empty, add a dummy term
        }
  
        solrQuery.setQuery(queryTermBuilder.toString());
        solrQuery.addFacetField("manufacturerName");
        solrQuery.addFacetField("CategoryUUIDLevelMulti");
        solrQuery.addFacetField("_spell");
        solrQuery.setFacetLimit(5);
        solrQuery.setFacetMinCount(1);
        
        addQueryConditions(searchIndexQuery, solrQuery);

        return solrQuery;
    }
    
    
    

}
