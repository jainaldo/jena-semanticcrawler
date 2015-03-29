package br.com.jena.sematiccrawler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Principal {
	public static final String RDF_FILE = "caminho do seu arquivo RDF ou sua URI";
	//exemplo de caminho do arquivo local: file://home/nome_do_usuario/test.rdf

	public static void main(String[] args) {
		Model graph = ModelFactory.createDefaultModel();
		SemanticCrawlerImpl crawlerImpl = new SemanticCrawlerImpl();
		crawlerImpl.search(graph, RDF_FILE);
		graph.write(System.out);

	}

}
