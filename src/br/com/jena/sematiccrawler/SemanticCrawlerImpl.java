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
		uriVisitadas.add(resourceURI);
		System.out.println("[Adicionado na lista] " + resourceURI);
		Model model = ModelFactory.createDefaultModel();
		model.read(resourceURI);
		StmtIterator tripasURISujeito = model.listStatements(
				model.createResource(resourceURI), (Property) null,(RDFNode) null);
		graph.add(tripasURISujeito);
		System.out.println("[Grafo] " + resourceURI);
		StmtIterator tripasSameAs = model.listStatements((Resource) null,OWL.sameAs, (RDFNode) null);
		if(tripasSameAs.hasNext()){
			for (Statement tripa : tripasSameAs.toList()) {
				System.out.println(">>>>> " + resourceURI + " <<<<<");
				Resource sujeito = tripa.getSubject();
				Resource objeto = (Resource) (tripa.getObject());
				if (sujeito.getURI().equals(resourceURI)) {
					if (objeto.isAnon()){
						noBrancoSearch(graph, model, objeto);
					}else{
						if (enc.canEncode(objeto.getURI())){
							if (uriVisitadas.contains(objeto.getURI())){
								System.out.println("[Uri Objeto, já foi visitada] " + objeto.getURI());
								continue;
							}else{
								System.out.println("[Dereferenciando -objeto]"+ objeto.getURI());
								try {
									search(graph, objeto.getURI());
								} catch (Exception e) {
									continue;
								}
							}
						} else {
							System.out.println("[Não - Derenfenciado devido Objeto] " + tripa);
							continue;
						}
					}
				} else {
					if (objeto.getURI().equals(resourceURI)) {
						if (sujeito.isAnon()){
							noBrancoSearch(graph, model, sujeito);
						}else{
							if (enc.canEncode(sujeito.getURI())){
								if (uriVisitadas.contains(sujeito.getURI())){
									System.out.println("[Uri Sujeito, já foi visitada] " + sujeito.getURI());
									continue;
								}else{
									System.out.println("[Dereferenciando - sujeito]"
											+ sujeito.getURI());
									try {
										search(graph, sujeito.getURI());
									} catch (Exception e) {
										continue;
									}
								}
							}else{
								System.out.println("[Não - derenfenciado devido sujeito] " + tripa);
								continue;
							}
						}
					} else {
						System.out.println("[Não - derenfenciado devido não está no padrão] "+ resourceURI+ " -> "+ tripa);
						continue;
					}
				}
			}
		}else{
			System.out.println("[Não tem sameAs] " + resourceURI);
		}
	}

	public void noBrancoSearch(Model graph, Model modelAtual, Resource noBranco){
		System.out.println("[NO EM BRANCO]" + noBranco.getId() );
		StmtIterator tripasNoBranco =  modelAtual.listStatements((Resource)noBranco.getId(), (Property) null, (RDFNode) null);
		graph.add(tripasNoBranco);

		for (Statement tripa : tripasNoBranco.toList()){
			Resource objeto = (Resource) (tripa.getObject());
			if (objeto.isAnon()){
				noBrancoSearch(graph, modelAtual, objeto);
			}
		}
	}

}
