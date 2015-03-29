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
		System.out.println("[  Adicionado na lista  ] " + resourceURI);
		Model model = ModelFactory.createDefaultModel();
		model.read(resourceURI);
		StmtIterator triplasURISujeito = model.listStatements(
				model.createResource(resourceURI), (Property) null,(RDFNode) null);
		graph.add(triplasURISujeito);
		System.out.println("[  Adicionado no Grafo  ] " + resourceURI);
		StmtIterator triplasSameAs = model.listStatements((Resource) null,OWL.sameAs, (RDFNode) null);
		if(triplasSameAs.hasNext()){
			for (Statement tripla : triplasSameAs.toList()) {
				System.out.println("[      VERIFICANDO TRIPLAS --- URI: "+ resourceURI +"     ]");
				Resource sujeito = tripla.getSubject();
				Resource objeto = (Resource) (tripla.getObject());

				if (sujeito.isAnon()){
					System.out.println("[  ANÔNIMO --- Sujeito  ] " + sujeito.getId());
					if (objeto.isAnon()){
						noBrancoSearch(graph, model, objeto);
						continue;
					}else if(objeto.getURI().equals(resourceURI)){
						noBrancoSearch(graph, model, sujeito);
						continue;
					}
				}else if(objeto.isAnon()){
					System.out.println("[  ANÔNIMO --- Objeto ] " + objeto.getId());
					if(sujeito.isAnon()){
						noBrancoSearch(graph, model, sujeito);
						continue;
					}else if (sujeito.getURI().equals(resourceURI)){
						noBrancoSearch(graph, model, objeto);
						continue;
					}
				}else if (sujeito.getURI().equals(resourceURI)){
					if (enc.canEncode(objeto.getURI())){
						if (uriVisitadas.contains(objeto.getURI())){
							System.out.println("[  URI VISITADA --- Objeto  ] " + objeto.getURI());
							continue;
						}else{
							System.out.println("[  DEREFERENCIANDO ---->> Objeto URI:  ] "+ objeto.getURI());
							try {
								search(graph, objeto.getURI());
							} catch (Exception e) {
								continue;
							}
						}
					}else{
						System.out.println("[  NÃO DEREFERENCIADO --- devido a URI do objeto  ] " + tripla);
					}
				}else if (objeto.getURI().equals(resourceURI)){
					if (enc.canEncode(sujeito.getURI())){
						if (uriVisitadas.contains(sujeito.getURI())){
							System.out.println("[  URI VISITADA --- Sujeito  ] " + sujeito.getURI());
							continue;
						}else{
							System.out.println("[  DEREFERENCIANDO ---->> Sujeito URI:  ] "+ sujeito.getURI());
							try {
								search(graph, sujeito.getURI());
							} catch (Exception e) {
								continue;
							}
						}
					}else{
						System.out.println("[  NÃO DEREFERENCIADO --- devido a URI do sujeito  ] " + tripla);
					}
				}else{
					System.out.println("[  NÃO DEREFERENCIADO --- devido a URI não está no padrão  ] "+ resourceURI+ " ----> "+ tripla);
				}
			}
		}else{
			System.out.println("[  NÃO TEM sameAs  ] " + resourceURI);
		}
	}

	public void noBrancoSearch(Model graphAtual, Model modelAtual, Resource noBranco){
		System.out.println("[  NO EM BRANCO  ] " + noBranco.getId());
		ArrayList<Statement> triplas = todasTriplasNoBrancoSujeito(noBranco, modelAtual);
		graphAtual.add(triplas);

		for(Statement tripla: triplas) {
			if (tripla.getObject().isAnon()){
				Resource objeto = (Resource)(tripla.getObject());
				System.out.println("[  VERIFICANDO TRIPLAS --- nó BRANCO  ] " + objeto.getId());
				noBrancoSearch(graphAtual, modelAtual, objeto);
				continue;
			}else{
				System.out.println("[  NÃO É NÓ BRANCO  ] " + tripla);
			}
		}
	}

	public ArrayList<Statement> todasTriplasNoBrancoSujeito(Resource noBranco, Model model){
		ArrayList<Statement> triplasNoBrancoSujeito = new ArrayList<>();
		StmtIterator triplas = model.listStatements();
		while(triplas.hasNext()){
			Statement tripla = triplas.nextStatement();
			Resource sujeito = tripla.getSubject();
			if (sujeito.equals(noBranco)){
				triplasNoBrancoSujeito.add(tripla);
			}
		}
		return triplasNoBrancoSujeito;
	}

}
