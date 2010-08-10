package it.unitn.disi.smatch.matcher.structure.tree.spsm;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.PropertyConfigurator;

import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

//TODO Juan, while it is fine to have "print" tests, please, make a real unit test out of it. Check in the older versions - I had here a couple of tests.
public class SPSMTest {
	
	private IMatchManager mm;
    String confPath = ".." + File.separator + "conf" + File.separator + "";//

    String propertiesFile = "s-match-spsm-function.properties";

//    String propertiesFile = "s-match-spsm.properties";
    //TODO Juan, remove unused stuff

    String logProperties = "logging.properties";
    String sourceFile = "source.txt";
    String targetFile = "target.txt";

    //TODO Juan, remove unused stuff
    String preprocesedSource = "source.xml";
    String preprocesedTarger = "target.xml";
    
    String filePath = ".." + File.separator + "test-data" + File.separator + "spsm" + File.separator + "";
    
    
    public static void main(String[] args) {
    	PropertyConfigurator.configure(".." + File.separator + "conf" + File.separator + "log4j.properties");
                
        SPSMTest ex = new SPSMTest();
        try {
//			ex.runMatcher();
			ex.testCases();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //exampleTabIndentedFile(mc);

//		"auto(brand,name, color)", "car(year,brand, colour)"
    }
    
    
    public void runMatcher() throws ConfigurableException {
        String configFile = confPath+ propertiesFile;
        
        mm = new MatchManager(configFile);
        String sourceFileName = filePath+sourceFile;
        String targetFileName = filePath+targetFile;

//        
//        mm.setProperties(configFile);


        //loads the files
        IContext sourceContext = mm.loadContext(sourceFileName);
        IContext targetContext = mm.loadContext(targetFileName);
        
        // linguistic pre-processing
        mm.offline(sourceContext);
        mm.offline(targetContext);

        // match
        IContextMapping<INode> mapping = mm.online(sourceContext, targetContext);
        mm.renderMapping(mapping, filePath + "result-default.txt");
        print(mapping);
    }
  
    
   private void matchFunctions(String fn1, String fn2){
	   try {
		   IContext sourceContext = mm.loadContext(fn1);
		   IContext targetContext = mm.loadContext(fn2);

		   // linguistic pre-processing
		   mm.offline(sourceContext);
		   mm.offline(targetContext);

		   // match
		   IContextMapping<INode> mapping = mm.online(sourceContext, targetContext);
		   mm.renderMapping(mapping, filePath + "result-default.txt");
		   print(mapping);
	   }
	   catch (Exception e){
		   e.printStackTrace();
	   }
   }
   


   

    public static void print(IContextMapping<INode> tm){
    	
    	Iterator<IMappingElement<INode>> me = tm.iterator();
        if (me != null){
        	//prints the root
	        System.out.println( "SIM: "+  tm.getSimilarity() );
	    	
	        //prints the rest
	        while(me.hasNext()){
	        	print(me.next());
	        }
	     
        }
        
    }

    
    public static void print(IMappingElement<INode> me){
	
        if (me != null){

	            System.out.println( "\t " +me.getSource().getNodeData().getName()+
	                    " <-> " + me.getTarget().getNodeData().getName()  +
	                    "\t {" + me.getRelation()+"}");
 	
        }
        
    }
    
    
    public void testCases(){
    	
        String configFile = confPath+ propertiesFile;
        try {
			mm = new MatchManager(configFile);
		} catch (SMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
        //29
//      matchFunctions( "retrieve_flood_level(ReporterID, Node, Day, Month, Year, Hour, Minute, WaterLevel, Unit)",  
//      "retrieve_flood_level(reporterID, node, day, month, year, grandstand_hour, minute, render_level, unit)");
      
      //31
//      matchFunctions( "retrieve_flood_level(ReporterID, Node, Day, Month, Year, Hour, Minute, WaterLevel, Unit)",  
//              "retrieve_flood_level( reporterID,  node,  day,  month,  annum,  hour,  minute,  nutrient_level,  girlish_unit)");
      
      
		
      matchFunctions("Courses(College of Arts and Sciences(Earth and Atmospheric Sciences,History(Latin America History,America History,Ancient European History),Computer Science))",
      "Course(College of Arts and Sciences(Earth Sciences(Geophysics,Geological Sciences),Computer Science,History(History of Americas,Ancient and Medieval History)))");

//              matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//                "retrieve_inundation_degree(ReporterID,Node,Timestep,WaterLevel)");
//
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_level(ReporterID,Node,Timestep,WaterLevel)");
//      
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_flood_level(ReporterID,Node,Timestep,Humidity)");
    
      
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_inundation_level(ReporterID,Node,Timestep,FloodLevel)");
      
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_flood_level(ReporterID,Node,Timestep,FloodLevel)");

//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_flood_level(ReporterID,Node,Timestep,RiverLevel)");
//      
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_flood_level(ReporterID,Node,Timestep,LakeLevel)");
      
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_flood_level(ReporterID,Node,Timestep,Level)");
//      
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel,UnitMeasure)");

//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_flood_level(CommunicatorID,Node,Timestep,WaterLevel)");
//
//
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel)",
//              "retrieve_flood_level(CommunicatorID,Node,Timestep,Level, Unit)");

   
      
//      matchFunctions( "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel,machine)",
//              "retrieve_inundation_level(ReporterID,Node,Timestep,WaterLevel,Scale,dog)"); 
//      matchFunctions( "retrieve_inundation_level(ReporterID,Node,Timestep,WaterLevel,Scale,dog)",
//              "retrieve_flood_level(ReporterID,Node,Timestep,WaterLevel,machine)");   
      
      
//      matchFunctions( "retrieve_element(Node,machine)",
//              "Retrieve_Element(Node,Timestep,Scale)"); 
//      matchFunctions( "Retrieve_Element(Node,Timestep,Scale)",
//              "retrieve_element(Node,machine)"); 

      
      
//      matchFunctions( "from(Newarker,demands_DuPont,Hong,Decatur_demage,unanimity_fascination,birth_enlisted,Quotations_influenced)",
//              "from(urban_ccenzer_dmage,Newarker,church,Quotations_influenced,Hong,biqgth_enlisted_extra,Bea_unanimity_fascination)");

      
      
      

//      matchFunctions( "find_Address_By_Point(dog,address_Finder_Options,part,find_Address_By_Point)",
//      "find_Address_By_Address(Finder,address_Finder_Options,Finder,find_Address_address_Point)");

//      matchFunctions( "qualify_liquor(thanked_deposit,flexibility_Matthew,Mariano,suddenly,thinking_districts_tense,treat_misery_strutting_Regency,latest_dollars_Gazette,exceeding_occasion)",
//      "qualify_liquor(thanked_deposit,flexibility_Matthew,Mariano,abruptly,thinking_districts_tense,treat_misery_strutting_Regency,latest_dollars_Gazette,trounce_occasion)");

      
      

//      matchFunctions( "GetWeatherText(GetWeatherTextSoapIn(GetWeatherText(ZipCode)),GetWeatherTextSoapOut(GetWeatherTextResponse(GetWeatherTextResult)))",
//              "ChangeAngleUnit(ChangeAngleUnitSoapIn(ChangeAngleUnit(AngleValue,Angles(Angles),Angles(Angles))),ChangeAngleUnitSoapOut(ChangeAngleUnitResponse(ChangeAngleUnitResult)))");
        
      
      
//      matchFunctions( "GetDirections(GetDirectionsRequest(FromState,FromCountry,ToCountry,ToAddress,FromAddress,ToCity,ToState,FromCity),GetDirectionsResponse(return))",
//            "getCodeByName(getCodeByNameRequest(arg0),getCodeByNameResponse(return))");
      		
//      matchFunctions( "GetPhoneInfo(GetPhoneInfoSoapIn(GetPhoneInfo(PhoneNumber,LicenseKey)),GetPhoneInfoSoapOut(GetPhoneInfoResponse(PhoneInfo(Providers(Provider(Name,URL,City,State)),Contacts(Contact(Name,Address,City,State,Zip)),Error))))",
//            "GetInfoResponse(GetInfo(GetInfoRequest),return))");
      
      
//      matchFunctions("SendMail(SendMailRequest(ToAddress,FromAddress,MsgBody,ASubject),SendMailResponse(return))",
//      		"getCallDetails(getCallDetails(password,callId,username),getCallDetailsResponse(CallDetails(callee,caller,topic,type,session,events(ArrayOf_Event))))");
      
     

//      matchFunctions("SendEmail(SendEmailInput(Acknowledgement,FromAddress,MsgBody,ToAddress,Subject,Priority,To,From),SendEmailResponse(ReturnCode))",
//      		"SendTextToFax(SendTextToFaxSoapIn(SendTextToFax(FromEmail,Subject,FaxNumber,BodyText,ToName)),SendTextToFaxSoapOut(SendTextToFaxResponse(SendTextToFaxResult)))");
      
		
		//TODO the mappings matrix returned from default tree matcher does not work properly, The sources array is all null
//      matchFunctions("IsAlive6Response(IsAlive(IsAlive6Request),return)",
//      "ListLiveCityCamsResponse(ListLiveCityCams(ListLiveCityCamsRequest),ListLiveCityCamsResponse(LiveCityCam(citycam(camid,city,location)))))");
      
// 	 	matchFunctions("VersionInfoSoapOut(VersionInfo(VersionInfoSoapIn),VersionInfoResponse(VersionInfoResult)))",
// 	    	 "Translate(TranslateRequest(str,uri,requestType,ema),TranslateResponse(pass))");
 	 
      
//      matchFunctions( "getAuto(brand)",
//              "GetInfoResponse(GetInfo(GetInfoRequest),return))");

      
//             matchFunctions("price(Item,Quality,Delivery_day,Price)" ,"price(Item,Quality,Delivery_day,Price)");
//      matchFunctions("price(Item,Quality,Delivery_day,Price)" ,"price(Item(pen),Quality,Delivery_day,Price)");
		
		//TODO this case contains a bug
//      matchFunctions("price(Item,Quality,Delivery_day,Price)" ,"price(Item,Quality(1),Delivery_day(2),Price)");
//      matchFunctions("price(Item(marker),Quality,Delivery_day,Price)" ,"price(Item(pen),Quality,Delivery_day,Price)");
//      matchFunctions("price(Item,Quality(1),Delivery_day,Price)" ,"price(Item,Quality(2),Delivery_day,Price)");
//      matchFunctions("price(Item,Quality(1),Delivery_day(1),Price)" ,"price(Item,Quality(1),Delivery_day(2),Price)");
//      matchFunctions("price(Item(pencil),Quality(1),Delivery_day(1),Price)" ,"price(Item(pen),Quality(2),Delivery_day(2),Price)");
//      matchFunctions("price(Item,Quality,Delivery_day,Price)" ,"price(Item(pen),Quality(2),Delivery_day(2),Price)");        
//    matchFunctions("require(model, quantity, year)","need(amount, model)");
   
//      matchFunctions("auto(brand,name, color)", "car(year,brand, colour)");
//      
//      
//      matchFunctions("require(auto,description,model,make)","require(auto,description,model,make)");
//      matchFunctions("require(auto,description,model,make)","need(car,make,model,year)");
//      matchFunctions("require(auto,make,color, make)","require(car,make,model)");     
//      matchFunctions("require(auto,auto,model,make)","need(car,make,model,year)");
//      matchFunctions("display(auto,model,make)","show(car,make,model,year)");
//      matchFunctions("require(car,bomb,make)","need(car,make)");
//      matchFunctions("afford(car, quantity, price)","afford(car,price)");
//       
//       
//     
//    matchFunctions("showEqual(Arg1,Arg2)","toknow(word)");
//      
//   matchFunctions(" get_wine(region,country,color,price,amount)",
//   "get_wine(region( country,area	), colour, cost, year, quantity)");//NOT RELATED. 1-(4,5/max(6,8))
//   
//      matchFunctions("auto(brand,name, year)", "car(brand, year,name)");
//      matchFunctions("auto(brand,name, aaaa, bbbb, cccccc, color, year)", "car(brand, year,model,color,name)");
//      matchFunctions("auto(year, color, name)", "car(colour, name)");
//      matchFunctions("auto(brand,name, color)", "car(color, name)");//problem with relation among brand < name
//      
//      matchFunctions("auto(brand,name, color)", "car(brand,year, colour, name)");
//      
//      matchFunctions("auto(brand,name, color)", "car(brand, year, model, colour)");
//      
//      
//      matchFunctions("dog( big, hirsute)", "canid(hairy, large)");
//      matchFunctions("display( word, definition)", "show( word,description)");
//      matchFunctions("auto(brand, colour, year)", "car( date(day,month,year),brand, color)", mc,p);//NR-----
//      
//      matchFunctions("auto(brand,year, color)", "car(brand,YEAR, colour)");//rel
//           
//      matchFunctions("cat(big,black)", "cat(big,white)");//rel
//
//      
//      matchFunctions("auto(brand,year, color)", "car(brand, model, colour)",mc, p);//rel--
//      
//      matchFunctions("auto(brand,year, color)", "car(brand, colour ,model)",mc, p);//NR
//      
//      matchFunctions("auto(brand,year, color)", "car(brand, YEAR)");//rel
//      
//      matchFunctions("auto(brand,year, color)", "car(brand, model)");//NR
//      
//      matchFunctions("auto(brand,year, color)", "car(brand, color)");//rel
//      
//      matchFunctions("auto(brand,year, color)", "car(brand, model, colour,type)");//NR
//      
//      matchFunctions("auto(brand,year, type, color)", "car(brand,model, colour,type)");
//      
//      matchFunctions("get_wine(region,country, color, price,number_of_bottles)",
//               "get_wine(region(country,area), colour, cost, year,quantity)");//NOT RELATED. 1-(4,5/max(6,8))       
//
//      matchFunctions("get_wine", "get_wine");
//
//      
//      matchFunctions("get_wine(region,country, color, price,amount)",
//              "get_wine(region(country,area), colour, cost,quantity)");//NOT RELATED. 1-(4,5/max(6,8))
//      
//              
//      matchFunctions("get_wine(region,country, color, price,number_of_bottles)",
//                 "get_wine(country,region, quantity , cost, colour)",mc, p);//rel
//      
//      matchFunctions("get_wine(region,country)",
//               "get_wine(country,region)");//rel
//      
//      matchFunctions("get_wine(region,country)",
//               "buy_wine(country,region)");//not related, buy!=get for Wordnet
//
//    matchFunctions("purchase_wine(quantity)",
//    		"buy_wine(quantity)");//rel,          
//     
//    
//      matchFunctions("get_wine(region,country, color, price,number_of_bottles)",
//                "buy_wine(country,region, quantity , cost, colour)",  mc, p);//NR
    }
    
   
}
