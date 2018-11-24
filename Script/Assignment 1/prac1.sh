echo -n Enter the first number: 
read a
echo -n Enter the second number:
read b

# Notice the use of '\' for splitting a statement into two lines

if ([ `expr $a % 2` = 0 ] && [ `expr $b % 2` != 0 ]) || ([ `expr $a % 2` != 0 ] \
 && [ `expr $b % 2` = 0 ]);then
	echo yes
else
	echo no
fi