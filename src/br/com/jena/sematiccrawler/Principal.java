package br.com.jena.sematiccrawler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Principal {
	public static final String RDF_FILE = "http://dbpedia.org/resource/Zico";

	public static void main(String[] args) {
		Model graph = ModelFactory.createDefaultModel();
		SemanticCrawlerImpl crawlerImpl = new SemanticCrawlerImpl();
		crawlerImpl.search(graph, RDF_FILE);
		graph.write(System.out);

	}

}
