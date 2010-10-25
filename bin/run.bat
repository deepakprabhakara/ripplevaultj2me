del .\vault\app\*.class
del .\vault\conn\*.class
del .\vault\db\*.class
del .\vault\dto\*.class
del .\vault\gui\*.class
del .\vault\util\*.class
del .\vault.jar
del C:\DEVTOOLS\WTK22\bin\vault.jar
del C:\DEVTOOLS\WTK22\bin\vault-obf.jar
echo "***** removed old crap *****"

PATH=$PATH;C:\DEVTOOLS\WTK22\bin;C:\DEVTOOLS\j2sdk1.4.2_10\bin
set MIDP_HOME=C:\DEVTOOLS\WTK22\bin;

javac -bootclasspath C:\DEVTOOLS\WTK22\lib\cldcapi11.jar;C:\DEVTOOLS\WTK22\lib\jsr75.jar;C:\DEVTOOLS\WTK22\lib\midpapi20.jar;C:\DEVTOOLS\WTK22\lib\wma11.jar @.\META-INF\javafiles
echo "***** compilation done *****"
jar cvfm vault.jar .\META-INF\MANIFEST.MF vault\app\*.class vault\conn\*.class vault\db\*.class vault\dto\*.class vault\gui\*.class vault\util\*.class res\img\*.png res\img\w128h128\*.png res\img\w208h208\*.png
echo "***** jar file created *****"
copy vault.jar C:\DEVTOOLS\WTK22\bin
copy vault.jad C:\DEVTOOLS\WTK22\bin
copy script.rgs C:\DEVTOOLS\WTK22\bin
copy retroguard.jar C:\DEVTOOLS\WTK22\bin 
cd\
cd DEVTOOLS\WTK22\bin\
dir vault.jar
java -classpath C:\DEVTOOLS\WTK22\bin\retroguard.jar;C:\DEVTOOLS\WTK22\wtklib\emptyapi.zip RetroGuard vault.jar vault-obf.jar script.rgs
echo "***** run done *****"
preverify -classpath C:\DEVTOOLS\WTK22\lib\cldcapi11.jar;C:\DEVTOOLS\WTK22\lib\jsr75.jar;C:\DEVTOOLS\WTK22\lib\midpapi20.jar;C:\DEVTOOLS\WTK22\lib\wma11.jar;. -d . vault-obf.jar
echo "***** preverify done *****"
copy vault-obf.jar vault.jar
echo MIDlet-Jar-Size: >> vault.jad
dir vault.jar
echo "***** updating JAR size in JAD done *****"
@pause
echo emulator -classpath vault.jar -Xdescriptor:vault.jad
cd\
cd C:\Deepak\workspace\J2ME\vaultJava\src
copy C:\DEVTOOLS\WTK22\bin\vault.jar .\
echo "***** DONE *****"

del .\vault\app\*.class
del .\vault\conn\*.class
del .\vault\db\*.class
del .\vault\dto\*.class
del .\vault\gui\*.class
del .\vault\util\*.class
