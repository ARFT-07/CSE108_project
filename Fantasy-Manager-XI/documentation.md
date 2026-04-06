# Fantasy-Manager-XI Documentation

## 1. Overview

`Fantasy-Manager-XI` is a JavaFX desktop application for football club management. It combines:

- A JavaFX client UI
- A socket-based transfer market server
- Shared serializable model objects used by both client and server
- Static resource bundles for screens, styling, media, and initial data

Core capabilities include:

- Club login
- Squad browsing and player detail viewing
- Listing players on the public transfer market
- Buying listed players directly
- Scouting players from other clubs
- Making, accepting, and rejecting private offers
- Displaying incoming offers on the club home screen
- Rendering a visual squad layout on a football pitch

The project follows a practical MVC-like structure:

- `FXML` files define views
- Controller classes handle UI behavior and network requests
- Model classes define serializable business data
- Server classes manage transfer logic and shared state

## 2. High-Level Architecture

### 2.1 Runtime Components

```text
JavaFX Client
  -> FXML views
  -> Controllers
  -> SessionManager
  -> NetworkThread

Transfer Server
  -> TransferMarketServer
  -> ClientHandler (one per client)
  -> TransferMarket
  -> OfferStore

Shared Model
  -> Player
  -> TransferOffer
  -> MarketMessage
  -> Other simple POJOs
```

### 2.2 Main Data Flow

```text
User action in UI
  -> Controller validates input
  -> Controller creates MarketMessage
  -> NetworkThread sends message over socket
  -> ClientHandler receives and dispatches request
  -> Server updates shared state
  -> Server sends response/update messages
  -> NetworkThread receives message
  -> Platform.runLater(...) updates SessionManager / controller
  -> Controller refreshes UI
```

## 3. Project Structure and File Inventory

### 3.1 Root-Level Files

| File | Purpose |
| --- | --- |
| `build.gradle.kts` | Gradle build file; configures Java 21, JavaFX 21, dependencies, application plugin, and jlink packaging. |
| `settings.gradle.kts` | Sets the Gradle root project name to `Fantasy-Manager-XI`. |
| `gradlew` | Unix shell Gradle wrapper launcher. |
| `gradlew.bat` | Windows Gradle wrapper launcher. |
| `.gitignore` | Git ignore rules for generated files and IDE output. |
| `Fantasy-Manager-XI.iml` | IntelliJ IDEA module metadata. |
| `src.zip` | Archived source snapshot; not used at runtime. |

### 3.2 Important Directories

| Directory | Purpose |
| --- | --- |
| `src/main/java` | Main Java source code for client, models, server, services, and utilities. |
| `src/main/resources` | FXML, CSS, media, data files, and images loaded by the application. |
| `.gradle` | Gradle cache and internal metadata. Generated. |
| `build` | Build outputs, compiled classes, packaged artifacts, and reports. Generated. |
| `out` | IDE-generated compiled output/resources. Generated. |
| `.idea` | IDE project settings. |
| `.git` | Git repository metadata. |

### 3.3 Java Source Files

#### Module Definition

| File | Purpose |
| --- | --- |
| `src/main/java/module-info.java` | Declares the Java module, required JavaFX/modules, exported packages, and opened packages for FXML reflection. |

#### Package `org.buet.fantasymanagerxi`

| File | Purpose |
| --- | --- |
| `HomeController.java` | Controller for the informational homepage screen with team list, league table, and video player. |
| `HomepageApplication.java` | JavaFX `Application` entry for launching the home/prehome scene directly. |
| `Launcher.java` | Thin launcher with `main(...)` that starts `LoginApplication`. |
| `LoginApplication.java` | JavaFX `Application` entry that loads the login scene. |
| `LoginController.java` | Handles login UI, starts `NetworkThread`, and establishes the user session. |
| `Login_SignupApplication.java` | JavaFX `Application` entry for the login/signup selection scene. |
| `Login_SignupController.java` | Navigation controller between login and signup screens. |
| `MyTeamController.java` | Renders the current squad on a pitch and bench layout, with formation switching and player swapping. |
| `NetworkThread.java` | Client-side socket reader/writer thread; bridges network messages to JavaFX controllers. |
| `PlayerDBController.java` | Displays the logged-in club squad as player cards and opens player details. |
| `PlayerDetailController.java` | Shows player details and allows listing a player on the transfer market. |
| `PreHomeController.java` | Main club dashboard after login; shows incoming offers and navigation to major features. |
| `ScoutPlayersController.java` | Shows players from other clubs and supports private transfer offers. |
| `SessionManager.java` | Global client session state store for network thread, club identity, scene history, and observable squad data. |
| `SignupController.java` | Local file-based signup logic for creating accounts. Currently separate from server login flow. |
| `TransferMarketController.java` | Displays public transfer market listings and handles player purchases. |
| `TransferRecord.java` | Incomplete transfer record class placeholder. |

#### Package `org.buet.fantasymanagerxi.model`

| File | Purpose |
| --- | --- |
| `LeagueTableEntry.java` | Simple model for a league table row. |
| `MarketMessage.java` | Serializable client-server message wrapper used for all requests and responses. |
| `Player.java` | Core player domain model shared by client and server. |
| `Team.java` | Simple team model used by the homepage demo/data screen. |
| `TransferMarket.java` | Server-side in-memory list of public transfer listings. |
| `TransferOffer.java` | Serializable model for private club-to-club offers. |
| `user.java` | Empty placeholder class. |

#### Package `org.buet.fantasymanagerxi.server`

| File | Purpose |
| --- | --- |
| `ClientHandler.java` | Per-client server worker that handles request dispatch and sends updates back to matching clubs. |
| `list.java` | Empty generic placeholder class. |
| `OfferStore.java` | Server-side offer repository for pending, accepted, rejected, and expired offers. |
| `TransferMarketServer.java` | Main server bootstrap; loads data, accepts sockets, and creates `ClientHandler` instances. |

#### Package `org.buet.fantasymanagerxi.service`

| File | Purpose |
| --- | --- |
| `AuthService.java` | Empty placeholder for future authentication logic. |
| `DataService.java` | Supplies mock team and league table data for the homepage screen. |

#### Package `org.buet.fantasymanagerxi.util`

| File | Purpose |
| --- | --- |
| `ClubRegistry.java` | Utility for converting between club codes and display names and comparing clubs safely. |
| `PlayerDataManager.java` | Legacy/static utility for parsing player files into memory and filtering them. |
| `SceneSwitcher.java` | Reusable JavaFX scene navigation helper that loads FXML, CSS, and stage icon. |

### 3.4 FXML Files

| File | Controller | Purpose |
| --- | --- | --- |
| `homepage-view.fxml` | `HomeController` | League/homepage with team list, table, and media content. |
| `login-view.fxml` | `LoginController` | Club login form. |
| `login_signup-view.fxml` | `Login_SignupController` | Entry selection between login and signup. |
| `my-team.fxml` | `MyTeamController` | Visual squad and formation screen. |
| `player-db.fxml` | `PlayerDBController` | Squad player card browser. |
| `player-detail.fxml` | `PlayerDetailController` | Detailed player profile and sell/list screen. |
| `prehome-view.fxml` | `PreHomeController` | Main club dashboard after login. |
| `scout-players.fxml` | `ScoutPlayersController` | Scouting and private-offer screen. |
| `signup-view.fxml` | `SignupController` | Account creation form. |
| `transfer-market.fxml` | `TransferMarketController` | Public transfer market listing screen. |

### 3.5 CSS Files

| File | Purpose |
| --- | --- |
| `homepage-style.css` | Main UI styling for post-login pages and general scenes. |
| `login_signupstyles.css` | Styling for the login/signup selection scene. |
| `login_style.css` | Styling for the login screen. |
| `signup_style.css` | Styling for the signup screen. |

### 3.6 Data and Documentation Resources

| File | Purpose |
| --- | --- |
| `Arsenal.txt` | Initial Arsenal squad data for the server. |
| `Chelsea.txt` | Initial Chelsea squad data for the server. |
| `Liverpool.txt` | Initial Liverpool squad data for the server. |
| `ManCity.txt` | Initial Manchester City squad data for the server. |
| `ManUtd.txt` | Initial Manchester United squad data for the server. |
| `Tottenham.txt` | Initial Tottenham squad data for the server. |
| `ValidLoginInfo.txt` | Server-side login credential file. |
| `players_db.txt` | Consolidated player database file; currently more aligned with legacy loading utilities. |
| `fixtures.json` | Static fixture data resource. |
| `standings.json` | Static standings data resource. |
| `fcb.mp4` | Video used by the homepage media panel. |
| `basic.txt` | Existing project documentation asset. |
| `midnightlayout.md` | Existing UI/documentation note. |

### 3.7 Images

#### Shared UI Images

| File | Purpose |
| --- | --- |
| `bg_card.png` | Background image for FIFA-style player cards in `MyTeamController`. |
| `football_field.png` | Pitch background for the visual squad screen. |
| `logo.png` | Application icon/logo. |
| `signup_login_page.png` | Decorative asset for login/signup-related scenes. |

#### Player Portrait Images

| File | Purpose |
| --- | --- |
| `bernardo_silva.png` | Portrait image for Bernardo Silva. |
| `ederson_moraes.png` | Portrait image for Ederson Moraes. |
| `erling_haaland.png` | Portrait image for Erling Haaland. |
| `jack_grealish.png` | Portrait image for Jack Grealish. |
| `jeremy_doku.png` | Portrait image for Jeremy Doku. |
| `josko_gvardiol.png` | Portrait image for Josko Gvardiol. |
| `julian_alvarez.png` | Portrait image for Julian Alvarez. |
| `kevin_de_bruyne.png` | Portrait image for Kevin De Bruyne. |
| `kyle_walker.png` | Portrait image for Kyle Walker. |
| `manuel_akanji.png` | Portrait image for Manuel Akanji. |
| `matheus_nunes.png` | Portrait image for Matheus Nunes. |
| `oscar_bobb.png` | Portrait image for Oscar Bobb. |
| `phil_foden.png` | Portrait image for Phil Foden. |
| `rodri.png` | Portrait image for Rodri. |
| `ruben_dias.png` | Portrait image for Ruben Dias. |
| `stefan_ortega.webp` | Portrait image for Stefan Ortega. |

## 4. Package and Class Reference

## 4.1 Package `org.buet.fantasymanagerxi`

### `Launcher`

- Role: Standard Java entry point for the client.
- Key variables: None.
- Methods:
  - `main(String[] args)`: Starts the JavaFX runtime by launching `LoginApplication`.

### `HomepageApplication`

- Role: Launches the prehome/home scene directly.
- Key variables: None.
- Methods:
  - `start(Stage stage)`: Loads `prehome-view.fxml`, applies CSS, loads the logo, and shows the stage.

### `LoginApplication`

- Role: Main JavaFX application used by `Launcher`.
- Key variables: None.
- Methods:
  - `start(Stage stage)`: Loads `login-view.fxml`, applies login CSS, loads the logo, and shows the stage.

### `Login_SignupApplication`

- Role: Alternate JavaFX entry for a login/signup menu scene.
- Key variables: None.
- Methods:
  - `start(Stage stage)`: Loads `login_signup-view.fxml`, applies CSS, loads the logo, and shows the stage.

### `SessionManager`

- Role: Global client-side session state holder.
- Key variables:
  - `networkThread`: Active `NetworkThread` used by the logged-in session.
  - `loggedInClubId`: Normalized club code, such as `CHELSEA`.
  - `loggedInClubName`: Human-readable club name, such as `Chelsea`.
  - `squad`: Shared `ObservableList<Player>` used by UI screens.
  - `currentScene`, `previousScene`: Scene history for navigation awareness.
- Methods:
  - `getNetworkThread()`: Returns the active client network thread.
  - `setNetworkThread(NetworkThread t)`: Replaces the active network thread.
  - `getLoggedInClub()`: Returns display name of the logged-in club.
  - `getLoggedInClubId()`: Returns normalized club code.
  - `getLoggedInClubName()`: Returns display name of the club.
  - `startSession(NetworkThread thread, String clubId, String clubName, List<Player> squad)`: Initializes session state after successful login.
  - `getSquad()`: Returns the shared observable squad list.
  - `setSquad(List<Player> s)`: Replaces squad contents while preserving the observable list instance.
  - `updateSceneHistory(String newScene)`: Tracks scene transitions.
  - `getPreviousScene(String fallback)`: Returns previous scene or fallback.
  - `getCurrentScene()`: Returns current scene ID.
  - `clearNavigationHistory()`: Clears scene history.
  - `logout()`: Logs out the current user.
  - `clearSession()`: Disconnects networking and resets all session state.

### `NetworkThread`

- Role: Client-side socket communication thread.
- Key variables:
  - `HOST`, `PORT`: Socket target configuration.
  - `socket`: TCP client socket.
  - `out`, `in`: Object streams used to send/receive `MarketMessage`.
  - `listener`: Current active controller that should receive messages.
- Methods:
  - `setListener(MessageListener listener)`: Assigns the current controller listener.
  - `NetworkThread(MessageListener listener)`: Constructor; stores the initial listener and marks the thread as daemon.
  - `run()`: Opens socket streams, receives messages in a loop, and dispatches them to the JavaFX thread.
  - `sendMessage(MarketMessage msg)`: Sends a message asynchronously on a short-lived background thread.
  - `disconnect()`: Closes the socket.
  - `applySessionStateUpdate(MarketMessage msg)`: Applies session-wide state updates, currently for `SQUAD_UPDATE`.
- Inner interface:
  - `MessageListener`: Contract for controllers that want network callbacks.
    - `onMessageReceived(MarketMessage msg)`
    - `onConnectionFailed(String reason)`

### `LoginController`

- Role: Handles club login and session startup.
- Key variables:
  - `clubDropdown`: Club selector.
  - `passwordField`: Password input.
  - `errorLabel`: Status/error output.
  - `loginBtn`: Login trigger and current scene node.
  - `networkThread`: Network client created during initialization.
- Methods:
  - `initialize()`: Populates club dropdown and starts the network thread.
  - `handleLogin()`: Validates user input and sends a `LOGIN` request.
  - `onMessageReceived(MarketMessage msg)`: Handles `LOGIN_OK`, `LOGIN_FAIL`, and `ERROR`.
  - `onConnectionFailed(String reason)`: Displays network failure in the UI.

### `Login_SignupController`

- Role: Simple scene navigator between login and signup views.
- Key variables: None.
- Methods:
  - `goingToLoginScene(ActionEvent actionEvent)`: Loads `login-view.fxml`.
  - `goingToSignupScene(ActionEvent actionEvent)`: Loads `signup-view.fxml`.

### `SignupController`

- Role: Local file-based signup implementation.
- Key variables:
  - `FILE_NAME`: Output text file for created accounts.
- Methods:
  - `CreateAccpunt(ActionEvent actionEvent)`: Validates entered fields, checks duplicates, writes a new username/password pair, and returns to the previous scene.
  - `isDuplicateUsername(String username)`: Checks if the username already exists in the local file.
  - `returnToPrevScene(ActionEvent actionEvent)`: Returns to the login/signup scene.
  - `showAlert(String title, String message)`: Shows feedback to the user.

### `PreHomeController`

- Role: Main club dashboard after login.
- Key variables:
  - `clubNameLabel`: Displays active club name.
  - `offersSummaryLabel`: Displays current offer summary.
  - `offersContainer`: Holds rendered offer cards.
- Methods:
  - `initialize()`: Sets club name, registers the screen as network listener, requests current offers, and renders empty state.
  - `movetoplayerDb(ActionEvent actionEvent)`: Navigates to squad player card database.
  - `movetoMyPlayers(ActionEvent event)`: Navigates to pitch/formation screen.
  - `movetoHomePage(ActionEvent event)`: Navigates to the homepage media/table screen.
  - `movetoTransferPage(ActionEvent event)`: Navigates to public transfer market.
  - `movetoScoutPlayers(ActionEvent event)`: Navigates to scouting/private-offer screen.
  - `handleLogout(ActionEvent event)`: Clears session and returns to login.
  - `onMessageReceived(MarketMessage msg)`: Handles offer list updates, offer status updates, squad sync, and errors.
  - `onConnectionFailed(String reason)`: Displays connectivity failures.
  - `renderOffers(List<TransferOffer> offers)`: Rebuilds the dashboard offer cards.
  - `handleOfferDecision(String offerId, boolean accept)`: Sends `ACCEPT_OFFER` or `REJECT_OFFER`.
  - `buildStatusText(TransferOffer offer)`: Converts status to UI text.
  - `formatTimeLeft(long expiresAtEpochMillis)`: Formats time remaining for pending offers.
  - `statusColor(TransferOffer.Status status)`: Returns CSS color string per status.
  - `showInfo(String message)`: Shows an information alert.

### `PlayerDBController`

- Role: Displays the current squad as a searchable/filterable card grid.
- Key variables:
  - `searchField`: Name search field.
  - `clubFilter`: Legacy field; current controller primarily filters by position and search.
  - `playerGrid`: Container for player cards.
  - `countLabel`: Result count.
  - `btnAll`, `btnGK`, `btnDEF`, `btnMID`, `btnFWD`: Position filter toggles.
  - `backBtn`, `marketBtn`: Navigation controls.
  - `positionGroup`: Toggle group for position buttons.
  - `currentPosition`: Current selected position filter.
  - `squad`: Shared observable squad from `SessionManager`.
  - `squadListener`, `weakSquadListener`: Auto-refresh hooks for squad changes.
- Methods:
  - `initialize()`: Normalizes image paths, registers listener, wires toggle/search behavior, and renders the initial grid.
  - `filterPosition(ActionEvent e)`: Updates the active position filter.
  - `refreshGrid()`: Filters squad data and rebuilds the player card UI.
  - `buildCard(Player p)`: Creates one player card node.
  - `loadPlayerImage(Player p)`: Loads player image from resources, file system, or placeholder.
  - `openDetail(Player p)`: Loads the player detail screen and injects the selected player.
  - `openMarket()`: Requests the market and transitions to the market screen when data arrives.
  - `onMessageReceived(MarketMessage msg)`: Handles market navigation, sell updates, squad updates.
  - `onConnectionFailed(String reason)`: Displays connectivity errors in the count label.
  - `goBack(ActionEvent event)`: Returns to prehome.

### `PlayerDetailController`

- Role: Shows one player's full profile and allows the owner to list the player for sale.
- Key variables:
  - `playerPhoto`, `playerName`, `playerClub`, `playerPosition`, `playerRating`: Header UI.
  - `detailPanel`: Detail/stat container.
  - `backBtn`, `sellBtn`: Navigation and action buttons.
  - `statusLabel`: Operation status text.
  - `currentPlayer`: Player currently shown.
- Methods:
  - `setPlayer(Player p)`: Injects the current player, registers the controller as listener, fills the UI, and configures the sell button.
  - `handleSell()`: Opens price input dialog, validates value, and sends `SELL_PLAYER`.
  - `onMessageReceived(MarketMessage msg)`: Handles `SELL_OK`, `SQUAD_UPDATE`, and `ERROR`.
  - `onConnectionFailed(String reason)`: Displays connectivity issues.
  - `showError(String message)`: Displays an error alert.
  - `sectionHeader(String title)`: Builds a styled section label.
  - `statRow(String label, String value)`: Builds a single detail row.
  - `transferBlock(String history)`: Renders transfer history text into UI rows.
  - `loadPhoto(Player p)`: Loads the player photo.
  - `goBack()`: Returns to the player database screen.

### `TransferMarketController`

- Role: Displays all public transfer listings and lets users buy listed players.
- Key variables:
  - `marketGrid`: Listing container.
  - `countLabel`: Listing count.
  - `clubLabel`: Logged-in club label.
  - `statusLabel`: Market status text.
  - `backBtn`: Navigation button.
  - `listings`: Current market snapshot.
- Methods:
  - `initialize()`: Registers listener, updates heading labels, and requests market data.
  - `refreshGrid()`: Rebuilds the market card grid.
  - `buildMarketCard(Player p)`: Creates a card for a listed player.
  - `confirmAndBuy(Player p)`: Confirms purchase and sends `BUY_PLAYER`.
  - `onMessageReceived(MarketMessage msg)`: Handles market updates, successful buys, errors, and squad sync.
  - `onConnectionFailed(String reason)`: Displays connectivity issues.
  - `loadPlayerImage(Player p)`: Loads player portrait.
  - `gotoPlayerDB(ActionEvent event)`: Navigates to player database.
  - `gotoHome(ActionEvent event)`: Navigates to prehome.

### `ScoutPlayersController`

- Role: Displays non-owned players and supports private transfer offers.
- Key variables:
  - `clubLabel`, `countLabel`, `statusLabel`: Status/header labels.
  - `searchField`: Search input.
  - `playerGrid`: Player card grid.
  - Position toggle buttons and `positionGroup`: Position filtering.
  - `scoutPlayers`: Current scoutable player snapshot.
  - `currentPosition`: Active filter.
- Methods:
  - `initialize()`: Sets labels, wires filters, registers network listener, and requests scoutable players.
  - `filterPosition(ActionEvent event)`: Updates the position filter.
  - `goHome(ActionEvent event)`: Navigates to prehome.
  - `goToMarket(ActionEvent event)`: Navigates to public market.
  - `refreshGrid()`: Filters scout data and rebuilds the card grid.
  - `buildCard(Player player)`: Creates a scouting/player-offer card.
  - `openOfferDialog(Player player)`: Collects an offer price and sends `MAKE_OFFER`.
  - `onMessageReceived(MarketMessage msg)`: Handles scout updates, offer success, offer status, squad sync, and errors.
  - `onConnectionFailed(String reason)`: Displays connection failures.
  - `showError(String message)`: Shows an error alert.
  - `loadPlayerImage(Player player)`: Loads player portraits.

### `MyTeamController`

- Role: Visual squad management screen with formation layout and drag-like swapping via clicks.
- Key variables:
  - `FORMATIONS`: Formation templates mapping formation name to pitch position rows.
  - FXML nodes: `pitchPane`, `subsPanel`, `formationBar`, `clubLabel`, `statusLabel`, `budgetValueLabel`, `backbtn`, `marketBtn`.
  - `currentFormation`: Active formation name.
  - `startingXI`, `substitutes`: Derived squad partitions.
  - `selectedPlayer`, `selectedFromPitch`: Selection state for swaps.
  - `pitchSlots`, `benchCards`: Rendered node references for highlighting.
  - `cardBgImage`, `fieldImage`: Cached image resources.
  - `squadListener`, `weakSquadListener`: Listen for shared squad updates.
- Methods:
  - `initialize()`: Loads images, configures background, subscribes to squad changes, builds formation buttons, and renders the current squad.
  - `distributeSquad()`: Splits the shared squad into starting XI and substitutes.
  - `positionOrder(Player p)`: Sorting helper by broad position category.
  - `buildFormationButtons()`: Creates formation buttons dynamically.
  - `styleFormationBtn(Button btn, boolean active)`: Applies button styling.
  - `switchFormation(String name)`: Changes formation and re-renders.
  - `reloadSquadView()`: Recomputes squad splits and re-renders pitch and bench.
  - `renderPitch()`: Places cards on pitch positions.
  - `buildFifaCard(Player p, String posLabel, boolean selected)`: Builds a custom-styled player card.
  - `buildMiniStats(Player p)`: Builds small stat blocks on cards.
  - `fmt(double v)`: Converts rating-like values to a bounded integer string.
  - `highlightCard(StackPane card, boolean highlight)`: Applies selected state to a pitch card.
  - `highlightBench(HBox card, boolean highlight)`: Applies selected state to a bench row.
  - `renderBench()`: Builds the substitute bench UI.
  - `buildBenchCard(Player p)`: Creates one bench entry.
  - `handlePitchClick(int slotIndex, Player player, StackPane card)`: Handles selection and swapping on the pitch.
  - `handleBenchClick(int benchIndex, Player player, HBox card)`: Handles selection and swapping on the bench.
  - `deselect()`: Clears selection state.
  - `clearAllHighlights()`: Resets all rendered highlight styles.
  - `setStatus(String msg)`: Updates status label.
  - `goBack()`: Returns to prehome.
  - `goToMarket()`: Opens the market screen.
  - `getCurrentStage()`: Returns the current JavaFX stage.
  - `shortName(String fullName)`: Returns shortened player name for cards.
  - `initials(String fullName)`: Returns initials fallback.
  - `posColor(String pos)`: Maps broad positions to colors.
  - `loadPlayerImage(Player p)`: Loads a player image resource.
  - `loadResource(String path)`: Generic classpath image loader.

### `HomeController`

- Role: Homepage screen controller showing a team list, league table, and video.
- Key variables:
  - `teamListView`: Team list control.
  - `leagueTableView`, `positionCol`, `teamCol`, `pointsCol`, `gdCol`: League table UI.
  - `highlightLabel`: Label for additional highlighting.
  - `videoPane`, `mediaView`: Video container and media renderer.
  - `mediaPlayer`: JavaFX media player for `fcb.mp4`.
  - `dataService`: Supplies mock teams and league table data.
- Methods:
  - `initialize()`: Configures team list, league table, media player, and lifecycle hooks.
  - `setupMediaPlayer()`: Loads video resource and creates the media player.
  - `attachMediaLifecycle()`: Releases media resources when the scene/window is destroyed.
  - `handlePlay()`: Starts playback.
  - `handlePause()`: Pauses playback.
  - `cleanupMediaPlayer()`: Stops and disposes media resources.
  - `setupTeamList()`: Binds team list items and cell rendering.
  - `setupLeagueTable()`: Binds table columns to `LeagueTableEntry` data.
  - `backToPreHome(ActionEvent actionEvent)`: Returns to prehome and cleans media resources.

### `TransferRecord`

- Role: Placeholder for transfer-history records.
- Key variables:
  - `fromClub`, `toClub`, `year`, `isLoan`.
- Methods:
  - None implemented yet beyond comments.

## 4.2 Package `org.buet.fantasymanagerxi.model`

### `MarketMessage`

- Role: Standard client-server envelope for every request and response.
- Key variables:
  - `type`: Enum indicating request/response kind.
  - `clubName`, `password`: Used for login and contextual data.
  - `playerId`, `offerId`: Used for player/offer-specific actions.
  - `price`: Used for sales and offers.
  - `payload`: Generic object payload containing lists or domain objects.
- Methods:
  - Constructor: `MarketMessage(Type type)`
  - Standard getters/setters for all fields.
- `Type` enum values:
  - Requests: `LOGIN`, `GET_MARKET`, `GET_SCOUT_PLAYERS`, `GET_OFFERS`, `SELL_PLAYER`, `BUY_PLAYER`, `MAKE_OFFER`, `ACCEPT_OFFER`, `REJECT_OFFER`
  - Responses/events: `LOGIN_OK`, `LOGIN_FAIL`, `SELL_OK`, `BUY_OK`, `MARKET_UPDATE`, `SCOUT_PLAYERS_UPDATE`, `OFFERS_UPDATE`, `OFFER_OK`, `OFFER_STATUS_UPDATE`, `SQUAD_UPDATE`, `ERROR`

### `Player`

- Role: Main player entity shared across the application.
- Key variables:
  - Identity and club: `name`, `club`, `position`, `nationality`
  - Personal data: `dob`, `foot`, `heightCm`, `weightKg`, `shirtNo`
  - Contract/statistics: `contractEnd`, `goals`, `assists`, `appearances`, `wagePw`, `marketValueM`, `rating`
  - Transfer/UI data: `transferHistory`, `imagePath`, `askingPrice`, `onMarket`
- Methods:
  - Default constructor: `Player()`
  - Standard getters/setters for every field:
    - `getName()/setName(...)`, `getClub()/setClub(...)`, `getPosition()/setPosition(...)`
    - `getNationality()/setNationality(...)`, `getDob()/setDob(...)`, `getFoot()/setFoot(...)`
    - `getContractEnd()/setContractEnd(...)`, `getShirtNo()/setShirtNo(...)`
    - `getHeightCm()/setHeightCm(...)`, `getWeightKg()/setWeightKg(...)`
    - `getGoals()/setGoals(...)`, `getAssists()/setAssists(...)`, `getAppearances()/setAppearances(...)`
    - `getWagePw()/setWagePw(...)`, `getMarketValueM()/setMarketValueM(...)`, `getRating()/setRating(...)`
    - `getTransferHistory()/setTransferHistory(...)`, `getImagePath()/setImagePath(...)`
    - `getAskingPrice()/setAskingPrice(...)`, `isOnMarket()/setOnMarket(...)`
  - `getPositionColor()`: Returns a color string used by card badges.

### `TransferOffer`

- Role: Private transfer offer entity.
- Key variables:
  - `offerId`
  - `playerId`, `playerName`, `playerClubId`, `playerClubName`
  - `offeringClubId`, `offeringClubName`, `targetClubId`, `targetClubName`
  - `price`, `status`, `decisionNote`
  - `createdAtEpochMillis`, `expiresAtEpochMillis`
- Methods:
  - Standard getters/setters for each field.
  - `isExpired()`: Returns `true` if the current system time is past the expiry time.
- `Status` enum:
  - `PENDING`, `ACCEPTED`, `REJECTED`, `EXPIRED`

### `TransferMarket`

- Role: In-memory server-side store of players listed publicly.
- Key variables:
  - `listings`: Internal synchronized list of listed players.
- Methods:
  - `addListing(Player p)`: Adds a player to the market.
  - `removeListing(String playerId)`: Removes a player by name.
  - `findById(String playerName)`: Finds a listed player by name.
  - `getListings()`: Returns a copy of the current listings.
  - `isEmpty()`: Returns whether the listing store is empty.

### `LeagueTableEntry`

- Role: View-model style object for the homepage table.
- Key variables:
  - `position`, `teamName`, `points`, `goalDifference`
- Methods:
  - Constructor: `LeagueTableEntry(int position, String teamName, int points, int goalDifference)`
  - `getPosition()`
  - `getTeamName()`
  - `getPoints()`
  - `getGoalDifference()`

### `Team`

- Role: Simple homepage team object.
- Key variables:
  - `name`: Team display name.
  - `players`: Demo `ObservableList<String>` of player names.
- Methods:
  - `Team(String name)`: Creates team and default dummy player list.
  - `generateDefaultPlayers()`: Adds placeholder players `Player 1` through `Player 15`.
  - `getName()`
  - `getPlayers()`

### `user`

- Role: Placeholder class for a future user model.
- Key variables: None.
- Methods: None.

## 4.3 Package `org.buet.fantasymanagerxi.server`

### `TransferMarketServer`

- Role: Main server bootstrap and data loader.
- Key variables:
  - `PORT`: TCP port, currently `5000`
  - `DEFAULT_TRANSFER_BUDGET`: Per-club starting budget
  - `clubSquads`: Map of club code to current squad
  - `loginCredentials`: Map of club code to password
  - `clubBudgets`: Map of club code to remaining transfer budget
  - `transferMarket`: Shared public listing store
  - `offerStore`: Shared private offer store
  - `allHandlers`: Connected clients for broadcasting targeted updates
- Methods:
  - `main(String[] args)`: Loads data, opens the server socket, accepts clients, and submits each handler to a cached thread pool.
  - `loadAllData()`: Loads squad files and initializes budgets.
  - `parsePlayers(InputStream is, String club)`: Parses a club data file into players.
  - `parsePlayer(String block, String club)`: Parses one player block into a `Player`.
  - `loadCredentials()`: Loads club credentials from `ValidLoginInfo.txt`.
  - `parseInt(String s)`, `parseDouble(String s)`: Safe parsing helpers.

### `ClientHandler`

- Role: Per-client server worker responsible for dispatching requests and routing updates.
- Key variables:
  - `socket`, `in`, `out`
  - `clubSquads`, `loginCredentials`, `clubBudgets`
  - `transferMarket`, `offerStore`, `allHandlers`
  - `loggedInClub`
- Methods:
  - Constructor: Injects socket and shared state references.
  - `run()`: Initializes streams and processes messages until disconnect.
  - `dispatch(MarketMessage msg)`: Switches on message type and routes to handlers.
  - `handleLogin(MarketMessage msg)`: Authenticates a club and returns squad data on success.
  - `handleGetMarket()`: Returns current public listings.
  - `handleGetScoutPlayers()`: Returns all players not belonging to the logged-in club.
  - `handleGetOffers()`: Returns incoming offers for the logged-in club.
  - `handleSellPlayer(MarketMessage msg)`: Removes a player from the seller squad and adds the player to the public market.
  - `handleBuyPlayer(MarketMessage msg)`: Buys a listed player if budget and ownership rules allow.
  - `handleMakeOffer(MarketMessage msg)`: Validates and stores a private offer.
  - `handleAcceptOffer(MarketMessage msg)`: Completes a private transfer and updates both clubs.
  - `handleRejectOffer(MarketMessage msg)`: Rejects a pending private offer.
  - `findPlayerInSquad(String clubId, String playerId)`: Finds a player in a club squad.
  - `broadcastMarketUpdate()`: Sends market snapshot to all connected clients.
  - `notifySquadUpdate(String clubName)`: Sends updated squad to matching club clients.
  - `notifyOffersUpdate(String clubId)`: Sends updated incoming-offer list to the target club.
  - `notifyOfferDecision(TransferOffer offer)`: Sends offer status result to the buyer and seller clubs.
  - `sendError(String message)`: Sends an `ERROR` message.
  - `sendMessage(MarketMessage msg)`: Synchronized write helper.

### `OfferStore`

- Role: Central in-memory repository for private transfer offers.
- Key variables:
  - `offersByTargetClub`: Map from target club code to list of offers directed to that club.
- Methods:
  - `addOffer(TransferOffer offer)`: Stores a new offer; if the same buyer already has a pending offer for the same player, the older offer is marked rejected as superseded.
  - `getOffersForClub(String clubId)`: Returns sorted copies of offers for one club.
  - `acceptOffer(String offerId)`: Marks an offer accepted and rejects other pending offers for the same player.
  - `rejectOffer(String offerId, String note)`: Rejects a pending offer with a note.
  - `expireOffer(String offerId, String note)`: Marks an offer expired.
  - `findOfferById(String offerId)`: Returns a safe copy of an offer.
  - `rejectOtherPendingOffersForPlayer(String playerId, String acceptedOfferId)`: Closes competing offers after an acceptance.
  - `purgeExpiredOffers()`: Converts overdue pending offers into `EXPIRED`.
  - `findOffer(String offerId)`: Internal search helper.
  - `copyOffer(TransferOffer original)`: Returns a detached copy to avoid exposing internal mutable state.

### `list<T>`

- Role: Placeholder generic class.
- Key variables: None.
- Methods: None.

## 4.4 Package `org.buet.fantasymanagerxi.service`

### `DataService`

- Role: Supplies demo/homepage data.
- Key variables:
  - `teams`: `ObservableList<Team>` used by homepage list view.
  - `leagueTable`: `ObservableList<LeagueTableEntry>` used by homepage table.
- Methods:
  - `DataService()`: Loads both collections.
  - `loadTeams()`: Creates static team list.
  - `loadLeagueTable()`: Creates static standings list.
  - `getTeams()`: Returns teams list.
  - `getLeagueTable()`: Returns league table list.

### `AuthService`

- Role: Placeholder for future authentication abstraction.
- Key variables: None.
- Methods: None.

## 4.5 Package `org.buet.fantasymanagerxi.util`

### `ClubRegistry`

- Role: Club code/display-name normalization utility.
- Key variables:
  - `CODE_TO_NAME`: Ordered map of club code to display text.
- Methods:
  - `toCode(String rawClub)`: Converts display names or raw text into normalized club codes.
  - `toDisplay(String rawClub)`: Converts a code or raw input into a display name.
  - `sameClub(String left, String right)`: Compares clubs after normalization.

### `PlayerDataManager`

- Role: Static loader/filter utility for a large player database file.
- Key variables:
  - `ALL_PLAYERS`: Global loaded player list.
  - `loaded`: Flag indicating whether loading has occurred.
- Methods:
  - `loadFromStream(InputStream is)`: Loads and parses player data from a text stream.
  - `parsePlayer(String block, String club)`: Parses one player text block.
  - `getClubs()`: Returns distinct club names from loaded data.
  - `filter(String club, String position, String search)`: Filters loaded players by club, position, and search text.
  - `isLoaded()`: Returns load status.
  - `parseInt(String s)`, `parseDouble(String s)`: Safe parsing helpers.

### `SceneSwitcher`

- Role: Common navigation helper for switching JavaFX scenes.
- Key variables: None.
- Methods:
  - `switchScene(String fxml, ActionEvent event, double width, double height)`: Loads FXML and switches scene using an event source.
  - `switchScene(String fxml, Node node, double width, double height)`: Same, but uses a direct node reference.

## 5. JavaFX UI Architecture

### 5.1 Screen-to-Controller Pattern

Each major screen uses:

- An `FXML` file to define layout
- A Java controller class to handle events and populate UI
- Optional CSS files for styling

Example:

```text
player-db.fxml
  -> PlayerDBController
  -> reads SessionManager.getSquad()
  -> renders player cards into FlowPane
```

### 5.2 UI State Sources

The client uses multiple state sources:

- `SessionManager`
  - current club identity
  - current network thread
  - shared observable squad list
- Controller-local lists
  - `listings` in `TransferMarketController`
  - `scoutPlayers` in `ScoutPlayersController`
  - `startingXI` / `substitutes` in `MyTeamController`

### 5.3 Observable Collections and Refresh Strategy

Current JavaFX collection usage:

- `SessionManager.squad` is an `ObservableList<Player>`
- `PlayerDBController` subscribes to squad changes via `WeakListChangeListener`
- `MyTeamController` also subscribes to squad changes and rebuilds the pitch/bench view
- `DataService` and `Team` use `ObservableList` for table/list controls

UI refresh style in this project is mostly:

```text
state changes
  -> clear container
  -> rebuild child nodes
  -> update labels/counts
```

This is used in:

- `PlayerDBController.refreshGrid()`
- `TransferMarketController.refreshGrid()`
- `ScoutPlayersController.refreshGrid()`
- `PreHomeController.renderOffers()`
- `MyTeamController.renderPitch()` and `renderBench()`

### 5.4 Bindings and Listeners

Used JavaFX binding/listener techniques:

- `textProperty().addListener(...)` for live search fields
- `sceneProperty()` / `windowProperty()` listeners for media cleanup in `HomeController`
- `ListChangeListener` on the shared squad list
- Table column cell value factories in `HomeController`

The project does not currently use:

- `Task`
- `Service`
- advanced property binding chains

Instead, it uses manual updates plus `Platform.runLater(...)` for thread-safe UI synchronization.

### 5.5 Scene Navigation

Navigation is mostly centralized through `SceneSwitcher`, which:

- records scene history in `SessionManager`
- loads the requested FXML
- applies the common CSS if available
- adds the app icon
- replaces the current stage scene

Some controllers still navigate manually using `FXMLLoader` and `Stage.setScene(...)`.

## 6. Threading Model

## 6.1 Threads Used

### Client Side

- JavaFX Application Thread
  - Owns all UI controls and scene graph updates
- `NetworkThread`
  - Long-lived daemon thread that reads server messages
- Per-send background threads
  - Created in `NetworkThread.sendMessage(...)` to avoid blocking the UI thread during writes

### Server Side

- Main server thread
  - Opens `ServerSocket` and accepts incoming connections
- Cached thread pool workers
  - One `ClientHandler` task per connected client

## 6.2 Why Threads Are Needed

- Socket reads block; they cannot run on the JavaFX Application Thread
- Socket writes may also block; they should not freeze the UI
- The server must support multiple clubs at the same time

## 6.3 Safe UI Concurrency

The client follows the main JavaFX rule:

> Only the JavaFX Application Thread may modify UI controls.

This is enforced in `NetworkThread.run()`:

```java
Platform.runLater(() -> {
    applySessionStateUpdate(msg);
    if (listener != null) {
        listener.onMessageReceived(msg);
    }
});
```

Effect:

- network I/O happens off the UI thread
- controller callbacks happen on the UI thread
- labels, panes, dialogs, and observable lists are updated safely

## 6.4 Server-Side Concurrency Controls

Current concurrency protections include:

- `TransferMarket` uses `synchronized` methods
- `OfferStore` uses `synchronized` methods
- `ClientHandler.sendMessage(...)` is synchronized per client
- `allHandlers` is a synchronized list

Important note for maintainers:

- `clubSquads`, `clubBudgets`, and `loginCredentials` are plain maps
- correctness currently relies on simple access patterns and the synchronized methods around surrounding business logic
- if the system grows, move shared mutable server state behind explicit locking or concurrent collections

## 6.5 Threading Diagram

```text
Client UI Thread
  -> handles button clicks
  -> renders UI

NetworkThread
  -> blocks on ObjectInputStream.readObject()
  -> passes messages to Platform.runLater(...)

Server Accept Thread
  -> accepts sockets
  -> submits ClientHandler to ExecutorService

ClientHandler Thread
  -> processes one client's requests
  -> mutates shared transfer/offer state
  -> sends targeted updates
```

## 7. Networking Architecture

## 7.1 Communication Model

- Protocol: Java object serialization over TCP sockets
- Client endpoint: `NetworkThread`
- Server endpoint: `TransferMarketServer` + `ClientHandler`
- Message format: `MarketMessage`

Streams are opened in the correct order:

1. `ObjectOutputStream`
2. `flush()`
3. `ObjectInputStream`

This avoids object stream handshake deadlocks.

## 7.2 Request/Response Pattern

Typical pattern:

```text
Controller creates MarketMessage
  -> NetworkThread.sendMessage(...)
  -> ClientHandler.dispatch(...)
  -> handler updates state
  -> handler sends response/update messages
  -> controller receives typed callback
```

## 7.3 Message Routing

Examples:

- `LOGIN` -> `LOGIN_OK` / `LOGIN_FAIL`
- `GET_MARKET` -> `MARKET_UPDATE`
- `SELL_PLAYER` -> `SELL_OK`, then `SQUAD_UPDATE`, then `MARKET_UPDATE`
- `BUY_PLAYER` -> `BUY_OK`, then seller/buyer `SQUAD_UPDATE`, then `MARKET_UPDATE`
- `MAKE_OFFER` -> `OFFER_OK`, then seller `OFFERS_UPDATE`
- `ACCEPT_OFFER` -> buyer/seller `SQUAD_UPDATE`, seller `OFFERS_UPDATE`, buyer/seller `OFFER_STATUS_UPDATE`

## 7.4 Data Ownership

- The server owns authoritative transfer and squad state
- The client maintains display state and cached snapshots
- `SessionManager` is the authoritative client-side session state holder

## 7.5 Networking Flow Example: Public Purchase

```text
TransferMarketController.confirmAndBuy()
  -> send BUY_PLAYER(playerName)
  -> ClientHandler.handleBuyPlayer()
    -> validate player exists
    -> validate not own club
    -> validate budget
    -> remove from market
    -> move player to buyer squad
    -> update budgets
    -> send BUY_OK to buyer
    -> notifySquadUpdate(buyer)
    -> notifySquadUpdate(seller)
    -> broadcastMarketUpdate()
  -> NetworkThread receives updates
  -> SessionManager squad changes
  -> MyTeamController / PlayerDBController refresh automatically
```

## 8. Core Feature Implementation

## 8.1 Login

Flow:

```text
LoginController.handleLogin()
  -> sends LOGIN with club + password
  -> ClientHandler.handleLogin()
  -> validates credentials from ValidLoginInfo.txt
  -> sends LOGIN_OK with squad payload
  -> LoginController.onMessageReceived()
  -> SessionManager.startSession(...)
  -> switch to prehome-view.fxml
```

Implementation notes:

- Login is club-based, not user-based
- Successful login populates the shared squad state

## 8.2 Selling a Player to the Public Market

Flow:

```text
PlayerDetailController.handleSell()
  -> dialog asks asking price
  -> sends SELL_PLAYER(name, price)
  -> ClientHandler.handleSellPlayer()
    -> removes player from seller squad
    -> marks player on market with asking price
    -> adds to TransferMarket
    -> sends SELL_OK
    -> notifies seller squad update
    -> broadcasts market update
  -> PlayerDBController / MyTeamController refresh from SessionManager
```

Business rules:

- Player must belong to the logged-in club
- Asking price must be positive

## 8.3 Buying a Player from the Public Market

Flow:

```text
TransferMarketController.confirmAndBuy()
  -> sends BUY_PLAYER(name)
  -> server validates market presence, ownership, and budget
  -> server moves player to buyer squad
  -> server updates budgets
  -> server notifies both squads and market listeners
```

UI sync:

- `NetworkThread.applySessionStateUpdate(...)` updates `SessionManager`
- `PlayerDBController` and `MyTeamController` react through `ObservableList` listeners

## 8.4 Scouting and Making Private Offers

Flow:

```text
ScoutPlayersController.initialize()
  -> sends GET_SCOUT_PLAYERS
  -> server returns all players not in current club

ScoutPlayersController.openOfferDialog()
  -> sends MAKE_OFFER with TransferOffer payload
  -> ClientHandler.handleMakeOffer()
    -> validates budget and ownership rules
    -> normalizes club IDs/names
    -> saves to OfferStore
    -> sends OFFER_OK to buyer
    -> notifies target club OFFERS_UPDATE
```

Offer behavior:

- Public-market players cannot receive private offers from the scouting screen
- Repeated offers from the same buying club for the same player are allowed
- The previous pending offer is marked superseded and the newer one becomes active

## 8.5 Accepting or Rejecting Offers

Flow:

```text
PreHomeController.handleOfferDecision()
  -> sends ACCEPT_OFFER or REJECT_OFFER
  -> ClientHandler.handleAcceptOffer() / handleRejectOffer()
  -> OfferStore changes offer status
  -> on acceptance:
    -> player moves seller -> buyer squad
    -> budgets update
    -> other pending offers for same player are rejected
  -> server notifies clubs
```

Acceptance rules:

- Only the target/selling club can decide
- Offer must still be pending
- Offer must not be expired
- Buyer must still have budget
- Player must still be owned by the selling club

## 8.6 UI Update Strategy After Transfers

Public and private transfers both rely on the same synchronization pattern:

```text
server changes authoritative squad
  -> sends SQUAD_UPDATE
  -> NetworkThread pushes update into SessionManager
  -> shared ObservableList changes
  -> ListChangeListener
  -> UI refresh
```

This is the main mechanism keeping:

- squad card screens
- my-team pitch screen
- player detail sell state
- scouting availability

consistent with backend state.

## 9. Notable Design Decisions

### 9.1 Shared Client Session Store

Using `SessionManager` avoids passing the squad and network thread manually between scenes.

Advantages:

- simpler scene transitions
- one observable squad source
- easy global access for logged-in club context

Trade-off:

- introduces global state
- requires discipline when testing and extending the app

### 9.2 Single Active Network Listener

The project currently uses one active controller listener at a time through:

- `NetworkThread.setListener(...)`

This works because the application is scene-based and typically only one screen is active. To reduce stale-state issues, session-level updates are applied centrally before controller callbacks.

Future improvement:

- replace the single-listener model with a small event bus or listener registry

### 9.3 Manual Scene Rebuilds

Most dynamic screens clear and rebuild cards instead of using custom list cells or virtualized controls.

Advantages:

- straightforward to understand
- easy to style card-based UIs

Trade-off:

- less efficient for very large datasets

## 10. Known Gaps and Maintenance Notes

The following items are important for maintainers:

- `AuthService`, `user`, `list`, and `TransferRecord` are placeholders/incomplete.
- `SignupController` is not integrated with the socket-based server login flow.
- `SignupController` writes to `ValidAccountinfo.txt`, while server login reads `ValidLoginInfo.txt`.
- `SignupController.returnToPrevScene()` references `loginsignupview.fxml`, but the actual file in resources is `login_signup-view.fxml`.
- `build.gradle.kts` still points `application.mainClass` to `org.buet.fantasymanagerxi.HelloApplication`, which is not present as an active class.
- `PlayerDBController.java` contains a large block of commented legacy code above the current implementation.
- Several static data files (`fixtures.json`, `standings.json`, `players_db.txt`) are present but not fully integrated into the main transfer workflow.

## 11. Extension Guidance

If you want to extend the system safely:

1. Add new network operations by extending `MarketMessage.Type`.
2. Implement request handling in `ClientHandler.dispatch(...)`.
3. Keep the server authoritative for all transfers and budget changes.
4. Push UI-facing state changes through `SQUAD_UPDATE`, `MARKET_UPDATE`, `OFFERS_UPDATE`, or a new dedicated update message.
5. Prefer updating `SessionManager` observable state instead of mutating screen-local lists only.
6. Keep all JavaFX UI changes on the JavaFX Application Thread.

## 12. Recommended Refactoring Targets

- Introduce dedicated service layers for transfer, offer, and authentication logic.
- Replace the single network listener with a publish-subscribe mechanism.
- Convert placeholder classes into real domain/service objects or remove them.
- Centralize budget visibility and expose it in the UI.
- Replace manual scene transitions in some controllers with consistent `SceneSwitcher` usage.
- Add unit/integration tests around `OfferStore`, `TransferMarket`, and `ClientHandler`.
- Consider JavaFX `Task`/`Service` for more structured client background work if networking grows more complex.

## 13. Quick Reference Diagrams

### 13.1 Client Layer Diagram

```text
FXML View
  -> Controller
    -> SessionManager
    -> SceneSwitcher
    -> NetworkThread
```

### 13.2 Server Layer Diagram

```text
TransferMarketServer
  -> accepts socket
  -> creates ClientHandler

ClientHandler
  -> reads MarketMessage
  -> updates TransferMarket / OfferStore / squad maps
  -> sends updates back to interested clubs
```

### 13.3 State Synchronization Diagram

```text
Server state changes
  -> MarketMessage update
  -> NetworkThread
  -> Platform.runLater(...)
  -> SessionManager ObservableList
  -> ListChangeListener
  -> UI refresh
```

## 14. Summary

This project is a JavaFX client-server football transfer system built around:

- scene-based JavaFX controllers
- a serializable message protocol
- a socket server with per-client handlers
- shared player/offer domain models
- observable client squad state for UI synchronization

The most important files for everyday development are:

- `SessionManager.java`
- `NetworkThread.java`
- `ClientHandler.java`
- `OfferStore.java`
- `TransferMarket.java`
- `PlayerDBController.java`
- `MyTeamController.java`
- `TransferMarketController.java`
- `ScoutPlayersController.java`
- `PreHomeController.java`

Together they define the state flow from server-side transfer decisions to client-side UI updates.
