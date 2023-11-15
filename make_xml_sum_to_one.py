"""
This script takes an xml_file and makes all the probabilities sum to 1.
You need to install bs4 library first (pip install bs4)

"""


from bs4 import BeautifulSoup

# Reading the data inside the XML file to a variable
xml_file = '/Users/parmis/Desktop/Bif_functor_road_scene_2.xml' # change the path to your xml path

with open(xml_file, 'r') as f:
    data = f.read()

# Parsing the XML using BeautifulSoup
Bs_data = BeautifulSoup(data, "xml")

# Finding all instances of the 'VARIABLE' tag
all_vars = Bs_data.find_all('VARIABLE')



# Counting all the possible values for a variable

print("Counting all the possible values for a variable")
var_to_num_values = {}
for var in all_vars:
    name = var.find('NAME')
    outcome_elements = var.find_all('OUTCOME')
    var_to_num_values[name.text] = len(outcome_elements)




all_defs = Bs_data.find_all('DEFINITION')



print("Making all the conditional probablities sum to 1")
for defin in all_defs:
    name = defin.find('FOR').text
    count_possible_values = var_to_num_values[name]

    table = defin.find('TABLE').get_text().split()
    table = list(map(float, table))
    new_table = []
    for i in range(0,len(table), count_possible_values):
        current_values = table[i: i + count_possible_values ]
        summ = sum(current_values)
        for i in range(len(current_values)):
            current_values[i] =  current_values[i] / summ
        new_table.extend(current_values)

    defin.find('TABLE').string = ' '.join([str(item) for item in new_table])

# Save the updated XML
with open(xml_file, 'w') as f:
    f.write(Bs_data.prettify())
print("File is updated!")
