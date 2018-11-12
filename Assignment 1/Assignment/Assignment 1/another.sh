i="Anik Sarker_3000562_assignsubmission_file_Asiignment1.zip"

var2="$(echo $i|cut -d'_' -f1)"
var3="$(grep -i "$var2" CSE_322.csv|wc -l)"
echo "$i $var2 $var3"