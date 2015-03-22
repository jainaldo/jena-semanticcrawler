package br.com.jena.sematiccrawler;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

public class SemanticCrawlerImpl implements SemanticCrawler {
	ArrayList<String> uriVisitadas = new ArrayList<>();
	CharsetEncoder enc = Charset.forName("ISO-8859-1").newEncoder();

	@Override
	public void search(Model graph, String resourceURI) {
		Model model = ModelFactory.createDefaultModel();
		model.read(resourceURI);
		StmtIterator tripasURISujeito = model.listStatements(
				model.createResource(resourceURI), (Property) null,
				(RDFNode) null);
		graph.add(tripasURISujeito);
		StmtIterator tripasSameAs = model.listStatements((Resource) null,
				OWL.sameAs, (RDFNode) null);

		if (uriVisitadas.contains(resourceURI)) {
			return;
		} else {
			uriVisitadas.add(resourceURI);
			System.out.println("[na lista]" + resourceURI);

			for (Statement tripa : tripasSameAs.toList()) {
				System.out.println("###### " + resourceURI + " #######");
				Resource sujeito = tripa.getSubject();
				Resource objeto = (Resource) (tripa.getObject());
				if ((sujeito.getURI().equals(resourceURI))
						&& (enc.canEncode(objeto.getURI()))) {
					System.out.println("[dereferenciado -objeto]"
							+ objeto.getURI());
					try {
						search(graph, objeto.getURI());
					} catch (Exception e) {
						continue;
					}
				} else {
					if ((objeto.getURI().equals(resourceURI))
							&& (enc.canEncode(sujeito.getURI()))) {
						System.out.println("[dereferenciado - sujeito]"
								+ sujeito.getURI());
						try {
							search(graph, sujeito.getURI());
						} catch (Exception e) {
							continue;
						}
					} else {
						System.err.println("[Não]" + tripa.getResource());
					}
				}
			}
		}
	}
}
