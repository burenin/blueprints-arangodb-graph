//////////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the Blueprints Interface for ArangoDB by triAGENS GmbH Cologne.
//
// Copyright triAGENS GmbH Cologne.
//
//////////////////////////////////////////////////////////////////////////////////////////

package com.tinkerpop.blueprints.impls.arangodb;

import java.util.Iterator;
import java.util.Vector;

import com.tinkerpop.blueprints.impls.arangodb.client.*;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Achim Brandt (http://www.triagens.de)
 * @author Johannes Gocke (http://www.triagens.de)
 */

public class ArangoDBVertexQuery implements VertexQuery {

    private final ArangoDBGraph graph;
    private final ArangoDBSimpleVertex vertex;
    private ArangoDBBaseQuery.Direction direction = ArangoDBBaseQuery.Direction.ALL;
    private Vector<String> labels = null;    
    private Long limit = null;    
    private ArangoDBPropertyFilter propertyFilter = new ArangoDBPropertyFilter();
    private boolean count;

    public ArangoDBVertexQuery(final ArangoDBGraph graph, final ArangoDBVertex vertex) {
    	this.graph = graph;
    	
    	if (vertex != null) {
    		this.vertex = vertex.getRawVertex();
    	}
    	else {
    		this.vertex = null;
    	}
        this.labels = new Vector<String>();
        this.count = false;
    }

    public VertexQuery has(final String key, final Object value) {
    	propertyFilter.has(key, value, ArangoDBPropertyFilter.Compare.EQUAL);
        return this;
    }

    public <T extends Comparable<T>> VertexQuery has(final String key, final T value, final Compare compare) {
        switch (compare) {
        case EQUAL:
        	propertyFilter.has(key, value, ArangoDBPropertyFilter.Compare.EQUAL);
        	break;
        case NOT_EQUAL:
        	propertyFilter.has(key, value, ArangoDBPropertyFilter.Compare.NOT_EQUAL);
        	break;
        case GREATER_THAN:
        	propertyFilter.has(key, value, ArangoDBPropertyFilter.Compare.GREATER_THAN);
        	break;
        case LESS_THAN:
        	propertyFilter.has(key, value, ArangoDBPropertyFilter.Compare.LESS_THAN);
        	break;
        case GREATER_THAN_EQUAL:
        	propertyFilter.has(key, value, ArangoDBPropertyFilter.Compare.GREATER_THAN_EQUAL);
        	break;
        case LESS_THAN_EQUAL:
        	propertyFilter.has(key, value, ArangoDBPropertyFilter.Compare.LESS_THAN_EQUAL);
        	break;
        }
        return this;
    }

    public <T extends Comparable<T>> VertexQuery interval(final String key, final T startValue, final T endValue) {
    	propertyFilter.has(key, startValue, ArangoDBPropertyFilter.Compare.GREATER_THAN_EQUAL);
    	propertyFilter.has(key, endValue, ArangoDBPropertyFilter.Compare.LESS_THAN);
        return this;
    }

    public VertexQuery direction(final Direction direction) {
    	if (direction == Direction.IN) {
            this.direction = ArangoDBBaseQuery.Direction.IN;    		
    	}
    	else if (direction == Direction.OUT) {
            this.direction = ArangoDBBaseQuery.Direction.OUT;    		
    	}
    	else {
            this.direction = ArangoDBBaseQuery.Direction.ALL;    		
    	}
        return this;
    }

    public VertexQuery labels(final String... labels) {
    	if (labels == null) {
    		return this;
    	}
    	this.labels = new Vector<String>();
    	
		for (String label: labels) {
			this.labels.add(label);
		}
    	
        return this;
    }

    public VertexQuery limit(final long max) {
        this.limit = max;
        return this;
    }
    
    
    public Iterable<Edge> edges() {    	
    	ArangoDBSimpleEdgeQuery query;
		try {
			if (vertex == null) {
				query = graph.client.getGraphEdges(graph.getRawGraph(), propertyFilter, labels, count);								
			}
			else {
				query = graph.client.getVertexEdges(graph.getRawGraph(), vertex, propertyFilter, labels, direction, limit, count);				
			}
			return new ArangoDBEdgeIterable(graph, query); 
		} catch (ArangoDBException e) {
			return new ArangoDBEdgeIterable(graph, null); 
		}    	
    }

    public Iterable<Vertex> vertices() {
    	ArangoDBSimpleVertexQuery query;
		try {
			if (vertex == null) {
				query = graph.client.getGraphVertices(graph.getRawGraph(), propertyFilter, count);								
			}
			else {
				query = graph.client.getVertexNeighbors(graph.getRawGraph(), vertex, propertyFilter, labels, direction, limit, count);
			}
			return new ArangoDBVertexIterable(graph, query); 
		} catch (ArangoDBException e) {
			return new ArangoDBVertexIterable(graph, null); 
		}    	
    }

    public long count() {
    	this.count = true;
    	long result = 0;

    	ArangoDBSimpleEdgeQuery query;
		try {
			if (vertex == null) {
				query = graph.client.getGraphEdges(graph.getRawGraph(), propertyFilter, labels, count);								
			}
			else {
				query = graph.client.getVertexEdges(graph.getRawGraph(), vertex, propertyFilter, labels, direction, limit, count);				
			}
			
			ArangoDBSimpleEdgeCursor cursor = query.getResult();
			
			result = cursor.count();
		} catch (ArangoDBException e) {
		}    	

    	this.count = false;
    	return result;
    }

    public Iterator<String> vertexIds() {
    	
    	return new Iterator<String>() {
    		
    		private Iterator<Vertex> iter = vertices().iterator();

    		public boolean hasNext() {
    			return iter.hasNext();
    		}

    		public String next() {
    			if (!iter.hasNext()) {
    				return null;
    			}
    			
    			Vertex v = iter.next();
    			
    			if (v == null) {
    				return null;
    			}
    			
    			
    			return v.getId().toString();
    		}

    		public void remove() {
    			iter.remove();
    		}
    		
    	};
    }

}
