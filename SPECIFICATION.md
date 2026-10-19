# SPECIFICATION.md — Weasis 4.7.3-1 (arm64 .deb extract)

Sumber: `~/Data/APPS/weasis` (extract .deb, bukan APK).
Paket: `weasis` 4.7.3-1, Section science, Arch arm64, Maintainer Weasis Team <Nicolas Roduit>, Lisensi EPL-2.0.
Upstream: https://github.com/nroduit/Weasis

## 1. Bukti analisis (REA + decompile)

| Bukti | Nilai |
|---|---|
| Target REA | `/Users/fahrurozyahmad/Data/APPS/weasis/opt/weasis/bin/Weasis` |
| Format | ELF executable arm64 |
| SHA-256 | `239b645d545a29f3f9e5c1b23141e35fb448867dd6ce4a36e1995fbc3c0e66f3` |
| Dokumen provider | `Weasis.hop` (Hopper, ev_48793a91f41c1749cec2df96482c3e5166c520d6ae3494fd79250d655b1365b4) |
| Procedures | 58 entri (ev_7c86ff1c3ad156a4307f26c0fc5f47b9f179688c0fc39b29f549ae8825892669) |
| Strings | 23 objek typed (ev_f003ef04e5235f626c6cd77b26e36b0e04f7caf053b6b72cb15e91b8aceae3af) |
| Dossier `main` | ev_83e19949a7946157eb1da605e0a5cb677310c1056655cfd919bea6fcfc0229ff, pseudocode 18703 char, signature `int main(int arg0..arg7)` |
| Callees `main` | 18 alamat (ev_86fe9a5011d5c10372b76a1f7a648ca0633526e69d7e2430d2c41ed238b78068) |
| Batas REA | `inspect_binary_layout` gagal (pwntools hanya Linux x64); `inspect_managed_artifact` gagal (target ZIP/JAR, butuh PE); `procedure_pseudo_code` timeout 300s (3x + unreachable) |
| Decompiler | CFR 0.152 (`/tmp/cfr.jar`), hasil di `.analysis/decompiled/{send,qr,codec,core,explorer,launcher}/`; `javap -p` untuk signature cepat |
| Biner kembar | `bin/Dicomizer` identik launcher (BuildID `d845b250fc567792eb65b2f935ce8f999bfe3a06`), beda `Dicomizer.cfg` |

## 2. Module Breakdown (yang harus dibuat ulang)

### 2.1 Native launcher (jpackage, C++)
- `bin/Weasis` @ `0x12a0` `main`: simpan argc/argv global (`appArgc`/`appArgv`), `sub_1080` init, `sub_10b0` cek, cabang JVM vs relokasi.
- `0x18a0` `jvmLauncherCreateJvmlLauncherData`, `0x1980` `jvmLauncherStartJvm`, `0x1b00` `getModulePath` (readlink `/proc/self/exe`), `0x1bd0` `popenCommand`, `0x1d8c` `findLauncherLib`, `0x1e20` `initPackageDesc.part.0.isra.0`, `0x1e40` `initDebPackage`, `0x1ea4` `initRpmPackage`, `0x1ef0` `getJvmLauncherLibPath`.
- `lib/libapplauncher.so`: `AppLauncher::launch/createJvmLauncher/createCfgFile`, `Package::initAppLauncher`, baca `Weasis.cfg/Dicomizer.cfg` (`app.mainjar/app.mainmodule/app.mainclass/java-options/AppCDSJavaOptions`).
- Strings kunci: `JPACKAGE_DEBUG`, `JLI_Launch`, `jvmLauncherGetAPI`, `jvmLauncherCreate`, `/proc/self/exe`, `/libapplauncher.so`, `rpm -ql '%s'`, `dpkg -L '%s'`, `rpm --queryformat '%{NAME}' -qf '%s'`, `dpkg -S '%s'`.
- Tidak ada logika DICOM/HTTP di native. Hanya bootstrap JVM + deteksi paket.

### 2.2 Java launcher + preferences (`weasis-launcher.jar`)
- `org.weasis.launcher.AppLauncher.main(String[] argv)`: log `*PERF*`, `PlatformCertificateLoader.startAsyncSetup()`, `Type.NATIVE`, `new ConfigData(argv)`, `Singleton.invoke/start`, `launch()`. Boot log `{home}/.weasis/log/boot.log` (rolling 3x3MB).
- `org.weasis.launcher.WeasisLauncher.launch(Type)`: `org.osgi.framework.storage + "-" + configData.getSourceID()`, `WeasisLoader`, cek `weasis.min.native.version`, shutdown hook, start Felix, `ServiceTracker CommandProcessor`, `weasis:ui -v`, eksekusi `pendingCommands`.
- `org.weasis.pref.ConfigData`: konstanta `P_WEASIS_VERSION/PROFILE/NAME/PATH/CODEBASE_URL/CONFIG_URL/PREFS_URL/USER/SHOW_DISCLAIMER/RESOURCES_URL`, `PARAM_CONFIG_URL=wcfg`, `PARAM_ARGUMENT=arg`, `PARAM_PROPERTY=pro`, `PARAM_CODEBASE=cdb`, `PARAM_AUTHORIZATION=auth`; `extractArgFromUri` (split `$`), `applyConfigParams` (wcfg→replace, cdb/auth/pro/arg), `applyLocalCodebase` (`{dir}/conf/base.json`), `filterConfigProperties`.
- `org.weasis.launcher.Singleton`: single-instance via `ServerSocket 127.0.0.1` port acak + lock file `id_port`, `newActivation(List<String>)` → `executeCommands` bila `sameUser && sameConfig`.
- `org.weasis.launcher.RemotePrefService`: `GET {weasis.pref.url}?user={enc}&profile={enc}`, `Accept: text/x-java-properties`, connect 7000ms / read 10000ms, `props.load(is)`.
- Kelas bantu: `WeasisLoader`, `AutoProcessor` (install/start bundle + i18n), `FileUtil.getAdaptedConnection`, `Utils`, `LookAndFeels`, `FlatWeasisTheme`, `PlatformCertificateLoader`, `WeasisMainFrame(MBean)`, `AppPreferences/Preference`.

### 2.3 OSGi framework + config
- `felix.jar` (Felix 7.0.5). `conf/base.json` 90 pref, `conf/dicomizer.json` 41 pref, `conf/base-shell.json` 1, `conf/non-dicom-explorer.json` 9, `conf/version-compatibility.json`.
- Urutan start (`felix.auto.start.N`): gogo.runtime(1)/command+shell(3)/bundlerepository+promise(4)/component+configadmin+prefs+scr(5)/miglayout+jaxb(7)/docking+core-img+zip4j+joml(10)/weasis-core(12)/imageio-codec(15)/opencv(23 install)/dicom-codec(30)/jackson(35)/dicom-explorer(40)/base-ui(60)/viewer2d+sr(70)/au+wave(75)/base-viewer2d(100)/send+isowriter+qr(110)/rt(115)/viewer3d+jogl(120)/jogl-native(121 install).
- Dicomizer override: startlevel 110, tanpa viewer2d/sr/au/ecg/qr/isowriter, tambah base-explorer+acquire-explorer(45)+acquire-editor(85), kirim saja(110).

### 2.4 Core model (`weasis-core-4.7.3.jar`)
- `org.weasis.core.api.media.data`: `TagW` (registry `tags`, `AtomicInteger idCounter`), `TagReadable/Taggable/TagUtil/TagView`, `MediaSeriesGroupNode` (tree root→patient→study→series), `MediaSeries/MediaElement/ImageElement/Series/SeriesThumbnail/Thumbnail`, `FileCache/NativeCache`, `SeriesImporter/MediaReader/Codec`.
- `org.weasis.core.api.explorer.model`: `TreeModel/DataExplorerModel/Tree/TreeModelNode`, `ObservableEvent(BasicAction)`.
- `org.weasis.core.api.net`: `HttpUtils` (SHARED_CLIENT, retry stale 3x backoff 50ms), `NetworkUtil` (HttpURLConnection fallback, redirect 301/302/303 max 3), `URLParameters`, `URIUtils`, `SocketUtil`, `RequestStallGuard/StallWatchdog`, `HttpStream/HttpResponseStream/ClosableURLConnection`.
- `org.weasis.core.api.net.auth`: `AuthMethod/AuthProvider/AuthRegistration/DefaultAuthMethod`, `OAuth2ServiceFactory`, `JavaNetHttpClient(+Provider/Config/MultipartEncoder)`, `OpenIdJsonTokenExtractor/OpenIdOAuth2AccessToken`, `AsyncCallbackServerHandler/AcceptCallbackHandler/AcceptCompletionHandler`, `BodySupplier/FileBodyPartPayload`.

### 2.5 DICOM (`weasis-dicom-*.jar`, dcm4che3)
- codec: `TagD extends TagW` (+`TagSeq`), `DicomSeries`, `DicomMediaUtils`, `param/DicomParam/DicomNode/AdvancedParams/ConnectOptions/CstoreParams/ListenerParams/TlsOptions/DicomState/DicomProgress`, `mf/Patient/Study/Series/SopInstance/ArcParameters/ArcQuery/WadoParameters/HttpTag/QueryResult/Manifest*`, `web/DicomStowRS/DicomStowConfig/ContentType/Multipart*`, `op/CFind/CGet/CMove/CStore`, `tool/DicomListener`.
- explorer: `DicomModel` (59KB, hierarchy + `LOADING_EXECUTOR`), `DicomSeriesHandler`, `LoadDicom/LoadLocalDicom/LoadDicomDir/LoadDicomObjects`, `LocalPersistence` (`getPluginPersistence`), `pr/DicomPrSerializer`, `tag/DicomFieldsView`, `wado/DownloadManager/LoadSeries`, `rs/RsQueryResult`, `mf/DicomModelQueryResult`.
- qr (24 class): `RsQuery(Callable<Boolean>)`, `RetrieveTask(ExplorerTask)`, `RetrieveContext/RetrieveSelection/RetrieveTree(Model)`, `DicomQrView/DicomQrFactory`, `LoadQrSeries/LoadWadoUriSeries`, `SearchParameters`, `GracefulCancel`.
- send (6 class): `StowRS extends DicomStowRS`, `SendDicomView/SendDicomFactory`, `Messages`, `HttpServerErrorException`.
- viewer: `weasis-dicom-viewer2d/viewer3d/sr/rt/au/wave`, `weasis-base-viewer2d`, `weasis-base-ui/explorer`, `weasis-acquire-explorer/editor`, `weasis-dicom-isowriter`, `weasis-imageio-codec`, `weasis-core-img-5.0.0`, `weasis-opencv-core-linux-aarch64-5.0.0-dcm`, `jogamp`.

### 2.6 Dependensi eksternal (bundled)
`docking-frames-1.1.7`, `jackson-annotations-2.22`, `jackson-core-2.22.1`, `jackson-databind-2.22.1`, `jaxb-osgi-4.0.3`, `jogamp-2.6.0`, `jogamp-linux-aarch64-2.6.0`, `joml-1.10.9`, `miglayout-core-11.4.3`, `miglayout-swing-11.4.3`, Felix `bundlerepository-2.0.10/configadmin-1.9.26/gogo.command-1.1.2/gogo.runtime-1.1.6/gogo.shell-1.1.4/prefs-1.1.0/scr-2.2.18`, `osgi.service.component-1.5.1`, `osgi.promise-8.1.0`, `zip4j-2.11.6`, plus via import: scribejava, slf4j/logback, FlatLaf, dcm4che3, xz (tukaani).
Sistem (`control` Depends): `libc6`, `xdg-utils`, `libstdc++6`, `libgcc1`. Runtime bundled Java 26.0.2 (`java.base/compiler/datatransfer/xml/prefs/desktop/logging/management/net.http/transaction.xa/sql`, `jdk.charsets/crypto.cryptoki/crypto.ec/jdwp.agent/localedata/management`).

## 3. Data Models & Types

### 3.1 Tag registry
```java
TagW { int id; String keyword, displayedName; TagType type; int vmMin, vmMax; Object defaultValue; }
TagType { STRING, TEXT, URI, DICOM_PERSON_NAME, DICOM_PERIOD, DICOM_SEX,
  DICOM_DATE, DICOM_TIME, DICOM_DATETIME, INTEGER, FLOAT, DOUBLE, BYTE, DICOM_SEQUENCE }
TagD extends TagW { VR vr; String privateCreatorID; boolean retired; }
```
`getTypeFromTag(tagID, vr)`: `vr.isIntType→INTEGER`; `isTemporalType→DA?DATE:TM?TIME:DATETIME`; `isStringType→DS?DOUBLE:PN?PERSON_NAME:UR?URI:AS?PERIOD:(tag==0x100040)?SEX:(LT|ST|UT)?TEXT:STRING`; `SQ→SEQUENCE`; `FD→DOUBLE`; `FL→FLOAT`; `SL|SS|UL|US→INTEGER`; else `BYTE`; `vr==null→STRING`.
`readValue`: cabang string/date/int/float/double/sequence/bytes × `vmMax>1` array vs tunggal. `DS→parseDS` (kosong=NaN), `IS→parseIS` (kosong=0), multi split `\`. `readTags()`: StAX `dataelements.xml` (XXE off), `tag hex x→0 parse16`, `VM a-b` (`n→Integer.MAX_VALUE`), `SQ→TagSeq`, default `UNKNOWN` untuk PatientID/PatientName/StudyUID/SeriesUID/Modality.

### 3.2 Hierarki explorer (in-memory)
```
root(MediaSeriesGroupNode) → patient(PatientPseudoUID) → study(StudyInstanceUID)
  → series(SeriesInstanceUID: DicomSeries) → instance(MediaElement/ImageElement)
```
Tag kunci: `PatientPseudoUID, ExplorerModel, WadoParameters, WadoInstanceReferenceList, SeriesLoading/Selected/Open/Focused, Thumbnail(Path), FilePath/FileName/CurrentFolder, SlicePosition, SuvFactor, WadoCompressionRate/WadoTransferSyntaxUID, DirectDownloadFile/Thumbnail, RootElement, PrDicomObject, PRLUTsExplanation`.

### 3.3 Manifest query (`mf`)
```java
Patient { Map<String,Study> studiesMap }  Study { Map<String,Series> seriesMap }
Series { SopInstance[] }  SopInstance { Attributes }
ArcParameters { archiveID, baseURL, additionalParameters, overrideDicomTagsList, webLogin, httpTaglist }
WadoParameters extends ArcParameters { requireOnlySOPInstanceUID: bool; wadoRS: bool; queryMode: QueryMode }
ArcQuery → ManifestSerializer { arcId, baseUrl, webLogin, requireOnlySOPInstanceUID, additionalParameters }
QueryResult/AbstractQueryResult/DefaultQueryResult; ManifestFormat {XML, JSON}
```

### 3.4 DICOM DIMSE params
```java
DicomParam { int tag; String[] values }  DicomNode { aeTitle, hostname, port, type, usageType, tsuid }
AdvancedParams { ConnectOptions, TlsOptions, ... }  ConnectOptions { connectTimeout, acceptTimeout }
DicomState { status: int; message; Attributes[] dicomRSP; DicomProgress progress }
DicomProgress { Attributes cmd; int completed/remaining/failed/warning }
C-FIND keys SERIES: StudyUID(0x20000D), SeriesUID(0x20000E), Modality, SeriesNumber, SeriesDescription, 0x210005(anon series tag)
```
Status: `0`=sukses semua, `45056`=sebagian gagal, `49152`=HTTP failure.

### 3.5 STOW config + HTTP
```java
DicomStowConfig { requestUrl (normalisasi → …/studies); contentType=APPLICATION_DICOM;
  userAgent="Weasis STOW-RS Client"; headers: Map; threadPoolSize=5;
  connectTimeout=10s; httpVersion=HTTP_1_1 }
URLParameters { headers: Map<String,String>; httpPost: bool; connectTimeoutMillis; inactivityTimeoutMillis }
HttpStream { responseCode, responseMessage, inputStream }  AuthResponse extends HttpStream (scribejava)
```

### 3.6 Auth
```java
AuthProvider { name, authorizationUri, tokenUri, revokeTokenUri, openId: bool }
AuthRegistration { clientId, clientSecret, scope, authorizationGrantType, clientCredentialsGrant: bool }
DefaultAuthMethod { uid, AuthProvider, AuthRegistration, token: OAuth2AccessToken, code: String, local: bool }
NO_AUTH_ID="5aa85854-8f1f-11eb-b339-d3daace59a05"
GOOGLE: auth https://accounts.google.com/o/oauth2/v2/auth, token https://oauth2.googleapis.com/token,
  revoke https://oauth2.googleapis.com/revoke,
  scope "https://www.googleapis.com/auth/cloud-healthcare https://www.googleapis.com/auth/cloudplatformprojects.readonly"
KEYCLOAK: buildKeycloakProvider(name, baseUrl, realm) → {base}/realms/{realm}/protocol/openid-connect/{auth,token,revoke}
CALLBACK_URL="http://127.0.0.1:" + (port | random)
```

### 3.7 Launcher prefs
```java
ConfigData { Properties properties; AppPreferences preferences; List<String> arguments;
  Map<String,String> felixProps; StringBuilder configOutput }
AppPreferences/Preference { code, value, description, type(A/F/AP), category, javaType }
```
Folder: `{home}/.weasis/preferences/{user}/{profile}/`, `weasis.properties`, Felix cache `${felix.cache.rootdir}/.weasis/cache-{sourceID}`.

## 4. Business Logic Flowchart / Pseudocode

### 4.1 Native `main` (REA ev_83e1…, callees ev_86fe…)
```
simpan argc→appArgc, argv→appArgv
sub_1080(&fd_pkg) ; if -1 → log + return 1
sub_10b0() → r19 ; if -1 → log + return 1
if r19 == 0:                       # jalur JVM langsung
  if fd_pkg >= 0: sub_11a0(fd_pkg) # (fd & 0x80000000)==0
  jvmPath = getJvmLauncherLibPath() # /proc/self/exe + dpkg/rpm popen
  h = dlopen(sub_10d0(jvmPath,2))
  getAPI = dlsym(h,"jvmLauncherGetAPI"); api = getAPI()
  create = dlsym(h,"jvmLauncherCreate"); data = create(argc,argv)
  jvmData = jvmLauncherCreateJvmlLauncherData(api,data,&pipe)
  tulis pipe: sub_11c0(fd, &argc,4); tulis ptr argv 8B; tulis blob argv N
  tiap gagal → jvmLauncherLog + r19=1
else:                              # jalur relokasi (r19>0)
  baca pipe sub_1230(fd,&n,4); malloc sub_1100(n); baca blob N
  tutup fd; sub_1220(unmap)
  delta = new-old; p' = p+delta; qword[1]+=delta
  loop i: *(arr[i]) += delta   # ubfiz idx*8, while(count!=i)
  h = dlopen; launch = dlsym(h,"JLI_Launch")
  r19 = jvmLauncherStartJvm(...) ; gagal → log + 1
tutup fd sisa; return r19
```
Matematika: `fd_valid = (fd & 0x80000000)==0`; `delta=new-old`; `p'=p+delta`; `offset(i)=i<<3`.

### 4.2 `AppLauncher.main → WeasisLauncher.launch`
```
log *PERF* INIT; PlatformCertificateLoader.startAsyncSetup()
configData = new ConfigData(argv)   # parse weasis://, wcfg/cdb/auth/pro/arg
if !Singleton.invoke(configData): AppLauncher.launch(NATIVE)
launch: storage = felix.storage + "-" + sourceID
  loader = loadProperties(); check weasis.min.native.version (dialog)
  shutdownHook; Felix.start(); ServiceTracker CommandProcessor
  executeCommands(pendingCommands + argv-args); gosh "weasis:ui -v"
```

### 4.3 QIDO-RS (`RsQuery.call/buildQueries`, CFR)
```
url = props["dicomweb.url"].rstrip("/") ; null → IllegalArgumentException
buf = url + "/studies?"
for i,q in queries: joiner=","; v in q.values (non-empty, URLEncoder UTF-8)
  if v: buf += hex(q.tag) + "=" + (PatientID split "^^^" → + "&00100021=" + issuer)
buf += STUDY_QUERY + props["query.ext"] + props["page.size"]
studies = RsQueryResult.parseJSON(buf, authMethod, URLParameters(queryHeaders))
for s in studies: if StudyUID(0x20000D) → populateDicomModel (patient→study node, writeMetaData)
```

### 4.4 Retrieve (`RetrieveTask.doInBackground`, CFR)
```
dest = comboDestination
if DefaultDicomNode:
  callingNode null → error no_calling_node
  params: connectTimeout=3000, acceptTimeout=5000
  CGET|CMOVE → queueSeriesRetrieve(C-FIND SERIES keys → LoadQrSeries)
  WADO → cari DicomWebNode WADO cocok host → WadoParameters → LoadWadoUriSeries
  else error msg_retrieve_type
elif DicomWebNode: fillSeries() # QIDO series per study + LoadSeries WADO-RS
CMOVE butuh DicomListener running (storageDir=StoreSCP) else sessionTemp
weasis.download.immediately, download.concurrent.series.images=4
```

### 4.5 STOW-RS (`StowRS.uploadDicom`, `DicomStowRS`, CFR)
```
multipart: Content-Type "multipart/related;type=application/dicom;boundary=weasisDicomBoundary",
  Accept XML, tiap file/direktori (rekursif) → FileBodyPartPayload(FileInputStream)
POST {requestUrl}/studies via HttpUtils.getHttpResponse(url, URLParameters(headers), authMethod, OAuthRequest POST)
code 200-399 → parse respons: 200→null (semua terkirim); 202|409→SAXReader.parse (sebagian/konflik); else throw
401 + authMethod → resetToken(); else buildHttpFailure (status 49152, log body 2048, dialog 200 char)
Java HttpClient: executor "DicomStow-*", Redirect.NORMAL, timeout 10s, HTTP_1_1
```

### 4.6 HTTP (`HttpUtils/NetworkUtil`, CFR)
```
noAuth (null|NO_AUTH): multipart? sendMultipart : GET/POST noBody + stale-retry(3, 50ms*i)
auth: OAuthRequest(GET|POST) + headers + User-Agent/Weasis-User → OAuth20Service.signRequest(token) → execute
NetworkUtil fallback: HttpURLConnection, sukses 200-299, redirect 301/302/303 max 3 (+Cookie forward),
  timeout UrlConnectionTimeout=5000 / UrlReadTimeout=15000
```

### 4.7 OAuth2 (`OAuth2ServiceFactory/DefaultAuthMethod`, CFR)
```
getService(authMethod, port=weasis.auth.back.port|0): key=uid:port → cache
registration clientCredentials? langsung requestClientCredentialsToken
else: port acak via SocketUtil.findAvailablePort → callback http://127.0.0.1:port
  openAuthorizationUrl → AsyncCallbackServerHandler tunggu code (POLLING_INTERVAL/TIMEOUT)
  → exchangeAndRefresh → OpenIdJsonTokenExtractor|OAuth2AccessTokenJsonExtractor
  → refreshExistingToken bila perlu; invalidateService(uid) hapus cache
```

### 4.8 Tag parse (`TagD`, CFR — lihat §3.1 untuk cabang VR/VM).

## 5. External Interfaces

### 5.1 CLI / scheme / desktop
- Exec: `/opt/weasis/bin/Weasis %U`, `/opt/weasis/bin/Dicomizer`. `StartupWMClass=org-weasis-launcher-AppLauncher`.
- Mime: `application/dicom` (`*.dcm`), `x-scheme-handler/weasis`. postinst: `xdg-desktop-menu install`, `xdg-mime install`; prerm: uninstall + hapus default handler.
- URI: `weasis://...$wcfg=..$cdb=..$auth=..$pro=k+v$arg=..` (split `$`, URL-decoded). Param: `wcfg` (ganti config URL), `cdb` (codebase), `auth`→`http.authorization`, `pro` (`k v`), `arg` (perintah gosh/`weasis:ui`).
- `Weasis.cfg`/`Dicomizer.cfg`: `app.classpath=$APPDIR/weasis-launcher.jar/felix.jar`, `app.mainclass=org.weasis.launcher.AppLauncher`; java-options: `-Djpackage.app-version=4.7.3`, `-Dgosh.port=17179`, `--enable-native-access=ALL-UNNAMED`, `-XX:MaxRAMPercentage=25`, `-XX:+UseStringDeduplication`, splash, `EmptyAccessibilityProvider`; Dicomizer tambah `-Dfelix.extended.config.properties=file:conf/dicomizer.json`.
- Env: `JPACKAGE_DEBUG`, `http.agent`, `http.bundle.cache`, `maven.localRepository`, `UrlConnectionTimeout/UrlReadTimeout`, `weasis.*` system properties override.

### 5.2 Filesystem
- Baca: `/opt/weasis/lib/app/{weasis-launcher.jar,felix.jar,bundle/*.jar.xz,bundle-i18n/*,conf/*.json,resources/*,Weasis.cfg,Dicomizer.cfg}`, `/opt/weasis/lib/{*.desktop,*.png,*.xml}`, JRE bundled.
- Tulis: `{home}/.weasis/cache-{sourceID}/` (Felix), `{home}/.weasis/preferences/{user}/{profile}/` + `weasis.properties`, `{home}/.weasis/log/boot.log(.%i.log.zip)`, session temp download, StoreSCP storageDir (C-MOVE), `resources.zip` bila `weasis.resources.url` berubah.
- Tanpa setuid/setgid. Tanpa SQLite (grep sqlite/jdbc/h2/derby nol).

### 5.3 IPC / loopback
- Singleton: `ServerSocket 127.0.0.1:<acak>` + lock file, protokol argumen teks antar instance.
- Gosh: `-Dgosh.port=17179`, `CommandProcessor.createSession(stdin/out/err)`, `execute("weasis:ui -v")`.
- OAuth callback: `http://127.0.0.1:{weasis.auth.back.port|acak}`.

### 5.4 DICOM DIMSE (TCP)
- Calling default (`resources/dicomCallingNodes.xml`): `WEASIS_AE@localhost:11113` BOTH, `WEASIS_QR@localhost:11113` RETRIEVE.
- Dicomizer default (`conf/dicomizer.json`): `weasis.acquire.dest.aet=DCM4CHEE`, `port=11112`, `host=null` (pilih node).
- Transfer capabilities (`resources/store-tcs.properties`): CT/MR/EnhancedCT/MR/PET, CR, DX, MG, US(+MultiFrame), XA/XRF(+Enhanced), NM, PET, VL Photo/VideoPhoto, WSI, ECG (12Lead/General/Ambulatory/Hemodynamic), GrayscaleSoftcopyPresentationState, BasicText/EnhancedSR, KeyObjectSelection, XRayRadiationDoseSR, EncapsulatedPDF/CDA/STL; `ImplicitVRLittleEndian;ExplicitVRLittleEndian` + varian JPEG/JPEG2000/JPEGLS/JPEGBaseline/MPEG2 sesuai modalitas.

### 5.5 DICOMweb (HTTP)
- QIDO-RS: `GET {dicomweb.url}/studies?{TAGHEX=val&...}{STUDY_QUERY}{query.ext}{page.size}` → JSON array Attributes. Series: `GET {base}/studies/{StudyUID}/series`.
- WADO-URI/RS: via `WadoParameters`; header `Accept: multipart/related;type=dicom;transfer-syntax=*`, thumbnail `image/jpeg`.
- STOW-RS: `POST {url}/studies`, `Content-Type: multipart/related;type="application/dicom";boundary=weasisDicomBoundary`, `Accept: <XML DicomContentType>`, `User-Agent: Weasis STOW-RS Client` + header custom; respons 200 OK semua / 202+XML sebagian / 409 konflik / 401 reset token.
- Umum: `User-Agent: {AppProperties.WEASIS_USER_AGENT}`, `Weasis-User: {WEASIS_USER}`, redirect NORMAL, proxy default, timeout connect 10s (STOW) / 5s+15s (umum) / 3s+5s (retrieve DIMSE).

### 5.6 Remote config / doc URL
- `GET {weasis.pref.url}?user=&profile=` (`Accept: text/x-java-properties`).
- Doc (bukan endpoint API): release notes GitHub `.../CHANGELOG.md#v473-2026-08-25`, `https://nroduit.github.io/en/{tutorials,basics/shortcuts,getting-started}`, flatlaf themes, IANA enterprise-numbers, DICOM PS3.6 NEMA, `TagW.java` GitHub.
- OAuth endpoint: Google + Keycloak template (lihat §3.6).

## 6. Catatan rekonstruksi
1. Native: tiru `main` + `getJvmLauncherLibPath` (proc/self/exe, dpkg/rpm probe, dlopen/dlsym, pipe protocol, relokasi delta). Jadikan satu file C + uji `ponytail:` stub JLI bila JVM absen.
2. Java launcher: `ConfigData` (URI/param/codebase) + `WeasisLauncher.launch` (Felix storage-sourceID, gosh) + `Singleton` + `RemotePrefService` dulu; UI belakangan.
3. Model: `TagW/TagD` (VR/VM/dataelements.xml) + tree `MediaSeriesGroupNode` + `DicomModel` + manifest `mf` sebelum viewer.
4. Jaringan: `HttpUtils/NetworkUtil/URLParameters` → QIDO (`RsQuery`) → WADO (`RetrieveTask/LoadSeries`) → STOW (`DicomStowRS/StowRS`) → auth (`OAuth2ServiceFactory/DefaultAuthMethod`).
5. Uji tiap lapis: assert native (delta/pipe), parse TagD (DS/IS/date/multi), QIDO URL builder, STOW multipart head, OAuth callback port acak. Tanpa framework bila 1 file cukup.
