@echo off
java -jar remapper\tiny-remapper-0.9.0-fat.jar target\InjectableJar-1.0-SNAPSHOT-shaded.jar remapped\InjectableJar_int.jar remapper\1.7.10.tiny named searge remapper\libs remapper\client_named.jar

java -jar remapper\tiny-remapper-0.9.0-fat.jar remapped\InjectableJar_int.jar remapped\InjectableJar.jar remapper\1.7.10.tiny searge obfuscated remapper\libs remapper\client_searge.jar

del remapped\InjectableJar_int.jar