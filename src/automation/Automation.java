package automation;

import java.util.List;

import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Oh_the_CAKE
 */
public class Automation extends JFrame implements ActionListener{

    /**
     * @param args the command line arguments
     */
    JButton processButton;
    JButton csvButton;
    JTextArea textArea;
    JScrollPane scroll;
    JComboBox statesDropdown;
    String totalForm;
    boolean firstLine;
    int idnum;
    
    public Automation() throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        /*
        set up the GUI
        */
        processButton = new JButton("Process form");
        csvButton = new JButton("Create .CSV");
        scroll = new JScrollPane();
        String[] listOfStates = {"OR"};
        statesDropdown = new JComboBox(listOfStates);
        textArea = new JTextArea    ("First: John\n" +
                                    "Middle: Jackson\n" +
                                    "Last: Smith\n" +
                                    "Street address: 123 Example St\n" +
                                    "City: Portland\n" +
                                    "State: OR\n" +
                                    "Zip code: 97201\n" +
                                    "Height: 6'-04\"\n" +
                                    "Weight: 180\n" +
                                    "Birthdate: 01-01-1993\n" +
                                    "Gender: M\n" +
                                    "Issue date(has to be before you turned 21): 01-01-2015\n" +
                                    "First licensed date (day you received your first license ever, typically when you turned 16): 01-01-13");
        
        setLayout(new java.awt.FlowLayout());
        setSize(500,485);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        textArea.setColumns(40);
        textArea.setRows(23);
        scroll.getViewport().add(textArea);
        add(scroll);
        add(processButton);processButton.addActionListener(this);
        add(csvButton);csvButton.addActionListener(this);
        add(statesDropdown);
        scroll.setVisible(true);
        firstLine=true;
        idnum=0;
        totalForm="";
        
    }
    
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        Automation display = new Automation();//create a Client
        display.setVisible(true);//make it visible
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        try {
            String selectedState = ((String)statesDropdown.getSelectedItem());//find out what state we are working with

            //if the button is the process button, then process the order form present
            if(evt.getSource()==processButton)
            {
                if(selectedState.equals("OR"))//check what state we want to make an ID for
                {
                    if(firstLine)
                    {
                        totalForm+="OR,CUSTOM_FILENAME_(OPTIONAL),2D_WIDTH,2D_HEIGHT,1D_WIDTH,1D_HEIGHT,DAQ,DAA,DAG,DAL,DAI,DAJ,DAK,DAR,DAS,DAT,DAU,DAW,DBA,DBB,DBC,DBD,ZOA\n";
                        firstLine=false;
                    }
                    totalForm += generateOregon();//convert the order form into a .CSV format String
                    idnum++;
                    JOptionPane.showMessageDialog(null, "Form processed successfully. ("+idnum+" IDs processed thus far)");
                }
                else if(selectedState.equals("SC"));
                else;
            }
            
            //File creation and naming/formatting of the name (It is named by State_Month_dayOfMonth-hour;minutes)
            if(evt.getSource()==csvButton)
            {
                Calendar cal = Calendar.getInstance();//make a calendar for the file name
                SimpleDateFormat sdf = new SimpleDateFormat("MMM_dd-hh;mm;ss");//format that calendar for the file name
                File file = new File(stateConverter(selectedState) +"_"+ sdf.format(cal.getTime()) + ".csv");//make a file with that state and the current time

                //writing the order form to the .CSV
                BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));//make a writer
                writer.write(totalForm);//write the formatted String for the given state into the file
                writer.close();//save the file
                firstLine = true; //reset the form for the next state we do
                idnum=0;
                totalForm = "";
                JOptionPane.showMessageDialog(null,".CSV generated successfully");
            }
            
            
            
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Automation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Automation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Automation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private String generateOregon() throws UnsupportedEncodingException, FileNotFoundException, IOException
    {
        //below sets the positions in the ArrayList of the order lines (the first line in the order form is the first name, the last line in the order form is when the user was first licensed...)
        int firstIndex=0;int middleIndex=1;int lastIndex=2;int streetIndex=3;int cityIndex=4;int stateIndex=5;
        int zipIndex=6;int heightIndex=7;int weightIndex=8;int birthdateIndex=9;int genderIndex=10;
        int issueIndex=11;int firstLicensedIndex=12;
        
        String result="";//this is the final result string
        
        //Oregon state input below
        
        ArrayList<String> finalForm = new ArrayList<>();//ArrayList for the form line by line
        //split the order form area into separate lines
        String[] form = this.textArea.getText().toUpperCase().trim().replaceAll("\\\\","").split("\\r?\\n");
        for(int i=0; i<form.length; i++)//go over each line
        {
            for(int j=0; j<form[i].length(); j++)//go over each character in that line
            {
                if(form[i].substring(j, j+1).equals(":") && form[i].substring(j+1) != null)
                {
                    finalForm.add(form[i].substring(j+1).trim());//eliminate the preceding information
                    break;//get out of the first loop and go to the next line in the order form
                }
            }
        }

        //fix the formatting of stuff and generate the DL number
        Random rand = new Random();//sets up for random use
        String c = ",";//condense the commas
        finalForm.set(heightIndex,finalForm.get(heightIndex).replaceAll("\\D+",""));//remove extra characters from the height
        finalForm.set(weightIndex,finalForm.get(weightIndex).replaceAll("\\D+",""));//remove extra characters from the weight
        int license = (rand.nextInt(9999999-1000000)+1000000);//make a random license number
        finalForm.set(genderIndex, this.fixGender(finalForm.get(genderIndex)));//format gender properly
        String expirationDate = createExpirationDate(finalForm.get(birthdateIndex),finalForm.get(issueIndex),8,true);
        finalForm.set(streetIndex,fixAddress(finalForm.get(streetIndex)));


        //set it all up into a long string
        String thisID ="OR"+c+"OR_ID_"+idnum+"_"+finalForm.get(lastIndex)+c+"5240"+c+"820"+c+"3000"+c+"550"+c+
                license+c+finalForm.get(firstIndex) + " " + finalForm.get(middleIndex) + " " + finalForm.get(lastIndex)+c+
                ""+c+finalForm.get(streetIndex)+c+finalForm.get(cityIndex)+c+stateConverter(finalForm.get(stateIndex))+c+finalForm.get(zipIndex)+c+
                "C"+c+"D"+c+""+c+finalForm.get(heightIndex)+c+finalForm.get(weightIndex)+c+expirationDate+c+
                finalForm.get(birthdateIndex)+c+finalForm.get(genderIndex)+c+finalForm.get(issueIndex)+c+
                "FIRST LICENSED: " + finalForm.get(firstLicensedIndex)+"\n";
        result+=thisID;//add this ID
        return result;//we are done adding IDs, so return what we have
    }
    
    private String stateConverter(String input)
    {
        String result;
        //next 2 lines make Lists of the states and their abbreviations
        List<String> longStates = Arrays.asList("Alabama","Alaska","Arizona","Arkansas","California","Colorado","Connecticut","Delaware","Florida","Georgia","Hawaii","Idaho","Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland","Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska","Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina","North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah","Vermont","Virginia","Washington","West Virginia","Wisconsin","Wyoming");
        List<String> shortStates = Arrays.asList("AL","AK","AZ","AR","CA","CO","CT","DE","FL","GA","HI","ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VT","VA","WA","WV","WI","WY");
        if(longStates.contains(input)){//if the , is not an abbreviation
            int index = longStates.indexOf(input);
            result = shortStates.get(index);}//change it to the abbreviated one
        //else if(shortStates.contains(input)){//if the state is an abbreviation
            //int index = shortStates.indexOf(input);
            //result = longStates.get(index);}//change it to an extended form
        else
            result = "NOT IN EXISTENCE";
        return result; 
    }
    
    private String fixGender(String input)
    {
        if(input.equalsIgnoreCase ("M") || input.equalsIgnoreCase("Male"))//return 1 for males
            return "1";
        else if(input.equalsIgnoreCase ("F") || input.equalsIgnoreCase("Female"))//otherwise return 2 for females
            return "2";
        else
            return "IRREGULAR GENDER/SEX";
    }
    
    private String createExpirationDate(String birthdate, String issuedate, int yearsToExpire, boolean onBirthday)
    {
        //fix to make the person 26
        String expirationDateMMDD = birthdate.replaceAll("\\D+","").substring(0,4);//get the first 4 digits of the birthdate
        int expirationDateYYYY = Integer.valueOf(birthdate.replaceAll("\\D+","").substring(4))+26;//get the year of the issue date, then add 8 to make the expiration year
        if(onBirthday)
            return (expirationDateMMDD+expirationDateYYYY);//combine them both to make the expiration date
        return "IRREGULAR EXPIRATION DATE";
    }
    
    private String fixAddress(String addressToFix)
    {
            String[] splitAdd = addressToFix.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < splitAdd.length; i++)
            {
                    if (splitAdd[i].contains("."))
                            splitAdd[i].replace(".", "");
                    sb.append(fixAddressHelper(splitAdd[i].toLowerCase())+" ");
            }
            return sb.toString().toUpperCase().trim();
    }
    
    private String fixAddressHelper(String word)
	{
		switch (word.toLowerCase())
		{
		case ("apartment"): word = "Apt";
		break;
		case ("avenue"): word = "Ave";
		break;
		case ("boulevard"): word = "Blvd";
		break;
		case ("building"): word = "Bldg";
		break;
		case ("center"): word = "Ctr";
		break;
		case ("circle"): word = "Cir";
		break;
		case ("corner"): word = "Cor";
		break;
		case ("court"): word = "Ct";
		break;
		case ("drive"): word = "Dr";
		break;
		case ("lane"): word = "Ln";
		break;
		case ("point"): word = "Pt";
		break;
		case ("road"): word = "Rd";
		break;
		case ("room"): word = "Rm";
		break;
		case ("street"): word = "St";
		break;
		case ("suite"): word = "Ste";
		break;
		case ("trail"): word = "Trl";
		break;
		}
		return word;
	}
    
    private String getFileName()//unused code for importinng an order form via .txt files
    {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("text files","txt");
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(filter);
        
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION){
            return fc.getSelectedFile().getPath();
        }
        else {
            return null;
        }
        
        /*    --NOT USED, BUT WOULD READ A FILE AND USE IT AS AN ORDER FORM--
            try(BufferedReader br = new BufferedReader(new FileReader(client.getFileName()))) {//get the file name and reader for it
                StringBuilder sb = new StringBuilder();//make a StringBuilder
                String line = br.readLine();
                while (line != null) {//basic
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                //String[] form = sb.toString().toUpperCase().trim().split("\\r?\\n");
            */
    }
    
}
