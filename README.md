# Projekt - Tworzenie Bezpiecznego Oprogramowania

## Opis
Celem projektu jest utworzenie procesu CI/CD implementującego mechanizmy bezpieczeństwa aplikacji.
Proces ma za zadanie zbudować i wypchnąć obraz dockerowy z aplikacją do zdalnego rejestru, ale
przed tym ma uruchomić testy aplikacji. Dopiero po pozytywnym wyniku testów może zostać zbudowany obraz
z aplikacja.

## Opis rozwiązania

### Aplikacja
Użyta została aplikacja z Laboratorium nr 1. Jest to prosta aplikacja w napisana
w języku Java przy użyciu frameworku Spring + Thymeleaf. Do aplikacji dołączony był plik Dockerfile
pozwalający zbudować obraz z aplikacją, który również będzie użyty w procesie CI/CD.
Część funkcjonalności (dodawanie nowych użytkowników) została celowo usunięta, aby w drugim etapie
móc zweryfikować poprawność działania procesu CI/CD.

### Repozytorium
Repozytorium jest publiczne. Posiada główna gałąź `main`. Dostęp do niej, tzn. pushe, merge requesty,
zmiany gałęzi mają tylko użytkownicy z uprawnieniami `Repository Admin`, w której jest tylko właściciel
repozytorium. Zabezpieczenia zostały utworzone za pomocą mechanizmu `Rulesets` w Github. Tworzenie innych
gałęzi jest umożliwione wszystkim użytkownikom repozytorium. Po każdym wypchnięciu commita do zdalnego
repozytorium uruchamiana jest akcja (Workflow) weryfikująca podany commit. Po poprawnej weryfikacji
tworzony jest obraz Dockerowy, który następnie zostaje umieszczony w zdanym repozytorium oprazów
hub.docker.com

### Uruchamiane testy
Podczas procesu CI/CD uruchamiane są cztery typy testów w następującej kolejności.

#### Testy jednostkowe i integracyjne
Tworzone razem z aplikacją w języku Java i uruchamiane za pomocą Maven.

#### Static code analysis
Realizowana za pomocą narzędzia Spotbugs i pluginu Find Security Bugs. Jest ono dołaczone do zależności
budowania aplikacji i jest uruchamiane z poziomu Maven. Obecna konfiguracja wykrywa tylko potencjalne
podatności bezpieczeństwa, jednak narzędzie ma szersze zastosowanie - m.in. może wykrywać złe praktyki
w pisaniu kodu

#### Static application security testing
Statyczne testy podatności w pakietach dołączoanych do aplikacji. Realizowane za pomocą narzędzia
OWASP Dependency-Check uruchamianego jako akcja Github. Skonfigurowane aby przerywać workflow
przy zagrożeniach typu critical (CSVV >= 7.0).

#### Dynamic application security testing
Realizowane przy pomocy narzędzia Zed Attack Proxy. Podczas budowania zostaje uruchomiony pełny skan,
który symuluje atak na aplikację o podanym adresie. Zarówno aplikacja, jak i kontener z testami zostają
uruchomione na tym samym workerze Github Actions i przeprowadzany jest atak. Narzędzie konfigurowane
jest z poziomu definicji workflow jako odrębna akcja. Obecnie skonfigurowane tak, aby przerywało workflow
dla każdego napotkanego zagrożenia. Jednak poprzez umieszczenie w repozytorium pliku konfiguracyjnego
można ustawić aby pewne zagrożenia były trakowane jako bardziej lub mniej poważne.

### Proces CI/CD

#### Workflow
Utworzone zostały cztery pipeline'y dla różnych gałęzi w repozytorium. Dwie z nich są docelowymi
pipeline'ami, które powinny zostać umieszczoe w repozytorium. Dwie pozostałe są utworzone aby można
było zaprezentować działanie workflow. Wszystkie budują aplikację i wypychają obraz docerowy do rejestru.

##### publish-docker-latest
Tylko dla gałęzi `main`. Buduje aplikację, uruchamia testy, buduje i wypycha obraz z tagiem `latest`.
##### publish-docker-beta
Dla wszystkich innych gałęzi z wyjątkiem dwóch poniższych. Buduje aplikację, uruchamia testy,
buduje i wypycha obraz z tagiem `beta`.
##### publish-docker-beta-dast-only
Tylko dla gałęzi `dast-only`. Buduje aplikację, uruchamia testy,
buduje i wypycha obraz z tagiem `beta`. Jednak testy SCA i zależności są ustawione tak aby nie
przerywały działania workflow.
##### publish-docker-vulnerable
Tylko dla gałęzi `vulnerable`. Buduje i wypycha obraz z tagiem `vulnerable`. Dla zobrazowania działania
budowania i wypychania obrazu z aplikacją.


#### Przebieg workflow

* Checkout repozytorium
* Ustawienie JDK na wersję 11
* Ustanienie cacheowania zależności dla poprawnie zbudowanej aplikacji w celu przyspieszenia
przebiegu workflow
* Zbudowanie aplikacji przy użyciu narzędzia Maven oraz uruchomienie testów SCA
* Uruchomienie testów zależności
* Wgranie raportu z testów zależności jako artefakt do obecnego przebiegu workflow
* Uruchomienie aplikacji na workerze
* Przeprowadzenie testów DAST
* Logowanie do rejestru hub.docker.com
* Zbudowanie i wypchnięcie obrazu do zdalnego rejestru
