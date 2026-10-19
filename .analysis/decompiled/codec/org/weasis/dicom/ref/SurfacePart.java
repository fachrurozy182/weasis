/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.ref;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.weasis.dicom.ref.AnatomicItem;
import org.weasis.dicom.ref.CodingScheme;
import org.weasis.dicom.ref.MesSurface;

public enum SurfacePart implements AnatomicItem
{
    ANTERIOR_TRIANGLE_OF_NECK(CodingScheme.SCT, 182329002, 41, 0, 42),
    CORNEA(CodingScheme.SCT, 28726007, 109, 0, 108),
    EYELASH(CodingScheme.SCT, 85803001, 105, 0, 104),
    FEMALE_EXTERNAL_URETHRAL_ORIFICE(CodingScheme.SCT, 279479008, 0, 504, 0),
    FRENULUM_OF_LABIA_MINORA(CodingScheme.SCT, 279867004, 0, 508, 0),
    GROIN_SKIN_CREASE(CodingScheme.SCT, 280387007, 519, 0, 518),
    HAIR(CodingScheme.SCT, 386045008, 0, 0, 0),
    IRIS(CodingScheme.SCT, 41296002, 109, 0, 108),
    MALE_EXTERNAL_URETHRAL_ORIFICE(CodingScheme.SCT, 279478000, 0, 513, 0),
    MUCOSA_OF_DORSUM_OF_ORAL_PART_OF_TONGUE(CodingScheme.FMA, 281534, 157, 0, 154),
    MUCOSA_OF_DORSUM_OF_PHARYNGEAL_PART_OF_TONGUE(CodingScheme.FMA, 281537, 155, 0, 152),
    MUCOSA_OF_FLOOR_OF_MOUTH(CodingScheme.SCT, 36152006, 161, 0, 158),
    MUCOSA_OF_LOWER_LIP(CodingScheme.SCT, 46353006, 165, 0, 162),
    MUCOSA_OF_MANDIBULAR_GINGIVA(CodingScheme.SCT, 245823002, 163, 0, 160),
    MUCOSA_OF_MAXILLARY_GINGIVA(CodingScheme.SCT, 245814000, 145, 0, 144),
    MUCOSA_OF_ORAL_SEGMENT_OF_HARD_PALATE(CodingScheme.FMA, 289677, 147, 0, 146),
    MUCOSA_OF_PALATOGLOSSAL_ARCH(CodingScheme.FMA, 60031, 151, 0, 148),
    MUCOSA_OF_PHARYNX(CodingScheme.FMA, 55031, 0, 0, 0),
    MUCOSA_OF_POSTERIOR_WALL_OF_OROPHARYNX(CodingScheme.FMA, 55060, 153, 0, 150),
    MUCOSA_OF_TIP_OF_TONGUE(CodingScheme.SCT, 245831007, 159, 0, 156),
    MUCOSA_OF_TONGUE(CodingScheme.SCT, 8001006, 0, 0, 0),
    MUCOSA_OF_UPPER_LIP(CodingScheme.SCT, 18444004, 143, 0, 142),
    MUCOSA_OF_UVULA(CodingScheme.FMA, 60030, 149, 0, 149),
    NAIL_UNIT_OF_FIFTH_TOE(CodingScheme.SCT, 770820003, 441, 0, 440),
    NAIL_UNIT_OF_FINGER(CodingScheme.SCT, 770809003, 0, 0, 0),
    NAIL_UNIT_OF_FOURTH_TOE(CodingScheme.SCT, 770821004, 439, 0, 438),
    NAIL_UNIT_OF_GREAT_TOE(CodingScheme.SCT, 770822006, 433, 0, 432),
    NAIL_UNIT_OF_INDEX_FINGER(CodingScheme.SCT, 770815003, 329, 0, 328),
    NAIL_UNIT_OF_LITTLE_FINGER(CodingScheme.SCT, 770818001, 335, 0, 334),
    NAIL_UNIT_OF_MIDDLE_FINGER(CodingScheme.SCT, 770816002, 331, 0, 330),
    NAIL_UNIT_OF_RING_FINGER(CodingScheme.SCT, 770817006, 333, 0, 332),
    NAIL_UNIT_OF_SECOND_TOE(CodingScheme.SCT, 770823001, 435, 0, 434),
    NAIL_UNIT_OF_THIRD_TOE(CodingScheme.SCT, 770825008, 437, 0, 436),
    NAIL_UNIT_OF_THUMB(CodingScheme.SCT, 770810008, 327, 0, 326),
    NAIL_UNIT_OF_TOE(CodingScheme.SCT, 770805009, 0, 0, 0),
    ORAL_MUCOSA(CodingScheme.SCT, 113277000, 0, 0, 0),
    POSTERIOR_COMMISSURE_OF_LABIUM_MAJORUM(CodingScheme.SCT, 0x3D533D, 0, 0, 0),
    RETINA(CodingScheme.SCT, 5665001, 0, 0, 0),
    SCLERA(CodingScheme.SCT, 18619003, 111, 0, 110),
    SKIN(CodingScheme.SCT, 39937001, 0, 0, 0),
    SKIN_OF_ABDOMEN(CodingScheme.SCT, 75093004, 0, 0, 0),
    SKIN_OF_ALA_NASI(CodingScheme.SCT, 68598004, 23, 0, 24),
    SKIN_OF_ANTECUBITAL_FOSSA(CodingScheme.SCT, 17957002, 303, 0, 302),
    SKIN_OF_ANTERIOR_HELIX_OF_EAR(CodingScheme.DCM, 130305, 119, 0, 118),
    SKIN_OF_ANTERIOR_PORTION_OF_NECK(CodingScheme.SCT, 11584001, 0, 60, 0),
    SKIN_OF_ANTERIOR_SURFACE_OF_FOREARM(CodingScheme.SCT, 70559009, 305, 0, 304),
    SKIN_OF_ANTERIOR_SURFACE_OF_KNEE(CodingScheme.SCT, 181553006, 405, 0, 404),
    SKIN_OF_ANTERIOR_SURFACE_OF_LOWER_LEG(CodingScheme.SCT, 25763004, 407, 0, 406),
    SKIN_OF_ANTERIOR_SURFACE_OF_THIGH(CodingScheme.SCT, 61248009, 403, 0, 402),
    SKIN_OF_ANTERIOR_SURFACE_OF_THORAX(CodingScheme.SCT, 244106003, 0, 0, 0),
    SKIN_OF_ANTERIOR_SURFACE_OF_UPPER_ARM(CodingScheme.SCT, 45981001, 301, 0, 300),
    SKIN_OF_ANTERIOR_TRUNK(CodingScheme.SCT, 181491009, 0, 0, 0),
    SKIN_OF_ANTITRAGUS(CodingScheme.SCT, 38407007, 123, 0, 122),
    SKIN_OF_ANUS(CodingScheme.SCT, 59112000, 0, 512, 0),
    SKIN_OF_AREOLA(CodingScheme.SCT, 72005009, 207, 0, 206),
    SKIN_OF_AXILLA(CodingScheme.SCT, 76261009, 355, 0, 354),
    SKIN_OF_BACK(CodingScheme.SCT, 66643007, 0, 0, 0),
    SKIN_OF_BACK_OF_TRUNK(CodingScheme.FMA, 49943, 0, 0, 0),
    SKIN_OF_BACK_OF_UPPER_THORACIC_REGION(CodingScheme.SCT, 699893008, 225, 0, 224),
    SKIN_OF_BUTTOCK(CodingScheme.SCT, 22180002, 231, 0, 230),
    SKIN_OF_CARUNCLE_OF_EYE(CodingScheme.DCM, 130306, 0, 0, 0),
    SKIN_OF_CAVITY_OF_CONCHA(CodingScheme.SCT, 51098001, 125, 0, 124),
    SKIN_OF_CHEEK(CodingScheme.SCT, 36141000, 13, 0, 14),
    SKIN_OF_CHIN(CodingScheme.SCT, 23747009, 35, 58, 36),
    SKIN_OF_CLITORIS(CodingScheme.SCT, 29353003, 0, 502, 0),
    SKIN_OF_CRUS_OF_HELIX(CodingScheme.SCT, 57726007, 117, 0, 116),
    SKIN_OF_DIGIT_OF_HAND(CodingScheme.SCT, 244169007, 0, 0, 0),
    SKIN_OF_DORSAL_AREA_OF_WRIST(CodingScheme.SCT, 52876008, 313, 0, 312),
    SKIN_OF_DORSAL_PART_OF_FIFTH_TOE(CodingScheme.FMA, 37885, 431, 0, 430),
    SKIN_OF_DORSAL_PART_OF_FOURTH_TOE(CodingScheme.FMA, 37882, 429, 0, 428),
    SKIN_OF_DORSAL_PART_OF_GREAT_TOE(CodingScheme.FMA, 37873, 423, 0, 422),
    SKIN_OF_DORSAL_PART_OF_INDEX_FINGER(CodingScheme.FMA, 38324, 325, 0, 324),
    SKIN_OF_DORSAL_PART_OF_LITTLE_FINGER(CodingScheme.FMA, 38333, 319, 0, 318),
    SKIN_OF_DORSAL_PART_OF_MIDDLE_FINGER(CodingScheme.FMA, 38327, 323, 0, 322),
    SKIN_OF_DORSAL_PART_OF_RING_FINGER(CodingScheme.FMA, 38330, 321, 0, 320),
    SKIN_OF_DORSAL_PART_OF_SECOND_TOE(CodingScheme.FMA, 37876, 425, 0, 424),
    SKIN_OF_DORSAL_PART_OF_THIRD_TOE(CodingScheme.FMA, 37879, 427, 0, 426),
    SKIN_OF_DORSAL_PART_OF_THUMB(CodingScheme.FMA, 38321, 317, 0, 316),
    SKIN_OF_DORSUM_OF_NOSE(CodingScheme.FMA, 59532, 19, 53, 20),
    SKIN_OF_EAR(CodingScheme.SCT, 1902009, 0, 0, 0),
    SKIN_OF_EAR_LOBULE(CodingScheme.SCT, 2059009, 131, 0, 130),
    SKIN_OF_EPIGASTRIC_AREA(CodingScheme.SCT, 30598005, 233, 0, 233),
    SKIN_OF_EXTERNAL_AUDITORY_CANAL(CodingScheme.SCT, 86409001, 0, 0, 0),
    SKIN_OF_EXTERNAL_GENITALIA(CodingScheme.SCT, 60944009, 0, 0, 0),
    SKIN_OF_EYE_REGION(CodingScheme.SCT, 362916000, 0, 0, 0),
    SKIN_OF_EYEBROW(CodingScheme.SCT, 367577003, 101, 0, 100),
    SKIN_OF_FACE(CodingScheme.SCT, 73897004, 0, 0, 0),
    SKIN_OF_FOOT(CodingScheme.SCT, 60496002, 0, 0, 0),
    SKIN_OF_FOREHEAD(CodingScheme.SCT, 68698007, 7, 52, 8),
    SKIN_OF_GLANS_PENIS(CodingScheme.SCT, 7991003, 511, 0, 511),
    SKIN_OF_GLUTEAL_FOLD(CodingScheme.SCT, 63029009, 0, 238, 0),
    SKIN_OF_HAND(CodingScheme.SCT, 33712006, 0, 0, 0),
    SKIN_OF_HEAD(CodingScheme.SCT, 70762009, 0, 0, 0),
    SKIN_OF_HEEL(CodingScheme.SCT, 84607009, 463, 0, 460),
    SKIN_OF_HELIX_OF_EAR(CodingScheme.SCT, 79313003, 0, 0, 0),
    SKIN_OF_HYPOGASTRIC_REGION(CodingScheme.SCT, 367578008, 235, 0, 235),
    SKIN_OF_HYPOTHENAR_REGION_OF_PALM(CodingScheme.SCT, 89784008, 343, 0, 342),
    SKIN_OF_INFERIOR_HELIX_OF_EAR(CodingScheme.DCM, 130307, 119, 0, 118),
    SKIN_OF_INFERIOR_POSTERIOR_SURFACE_OF_THE_PINNA(CodingScheme.DCM, 130308, 139, 0, 138),
    SKIN_OF_INFRAALAR_GROOVE(CodingScheme.DCM, 130312, 25, 0, 26),
    SKIN_OF_INFRACLAVICULAR_REGION(CodingScheme.SCT, 66288003, 203, 0, 202),
    SKIN_OF_INGUINAL_REGION(CodingScheme.SCT, 39687006, 223, 0, 222),
    SKIN_OF_INTERTRAGAL_INCISURE(CodingScheme.SCT, 45591000, 129, 0, 128),
    SKIN_OF_JAWLINE(CodingScheme.SCT, 244097004, 37, 0, 38),
    SKIN_OF_LABIUM(CodingScheme.SCT, 73058008, 0, 0, 0),
    SKIN_OF_LABIUM_MAJUS(CodingScheme.SCT, 128252004, 0, 0, 0),
    SKIN_OF_LABIUM_MINUS(CodingScheme.SCT, 128253009, 515, 0, 514),
    SKIN_OF_LATERAL_ASPECT_OF_ANKLE(CodingScheme.SCT, 181564009, 415, 0, 414),
    SKIN_OF_LATERAL_BORDER_OF_SOLE_OF_FOOT(CodingScheme.SCT, 35739000, 461, 0, 462),
    SKIN_OF_LATERAL_CANTHUS(CodingScheme.SCT, 37671003, 169, 0, 166),
    SKIN_OF_LATERAL_PART_OF_DORSUM_OF_FOOT(CodingScheme.DCM, 130309, 419, 0, 418),
    SKIN_OF_LATERAL_PART_OF_HEEL(CodingScheme.SCT, 699909001, 417, 0, 416),
    SKIN_OF_LATERAL_PORTION_OF_NECK(CodingScheme.SCT, 5272005, 43, 0, 44),
    SKIN_OF_LIP(CodingScheme.SCT, 88089004, 0, 0, 0),
    SKIN_OF_LOWER_ABDOMEN(CodingScheme.SCT, 699914002, 221, 0, 220),
    SKIN_OF_LOWER_ANTIHELIX_OF_EAR(CodingScheme.DCM, 130310, 0, 0, 0),
    SKIN_OF_LOWER_BACK(CodingScheme.SCT, 113182001, 229, 0, 228),
    SKIN_OF_LOWER_CHEST_WALL(CodingScheme.SCT, 699915001, 217, 0, 216),
    SKIN_OF_LOWER_EXTREMITY(CodingScheme.SCT, 371304004, 0, 0, 0),
    SKIN_OF_LOWER_EYELID(CodingScheme.SCT, 40069000, 115, 0, 114),
    SKIN_OF_LOWER_EYELID_MARGIN(CodingScheme.DCM, 130311, 113, 0, 112),
    SKIN_OF_LOWER_INNER_QUADRANT_OF_BREAST(CodingScheme.FMA, 61427, 213, 0, 212),
    SKIN_OF_LOWER_LIP(CodingScheme.SCT, 66934001, 0, 0, 0),
    SKIN_OF_LOWER_OUTER_QUADRANT_OF_BREAST(CodingScheme.FMA, 61423, 215, 0, 214),
    SKIN_OF_LOWER_PARASPINAL_REGION(CodingScheme.DCM, 130304, 0, 236, 0),
    SKIN_OF_MEDIAL_ASPECT_OF_ANKLE(CodingScheme.SCT, 181563003, 443, 0, 442),
    SKIN_OF_MEDIAL_BORDER_OF_SOLE_OF_FOOT(CodingScheme.SCT, 52953006, 459, 0, 458),
    SKIN_OF_MEDIAL_CANTHUS(CodingScheme.SCT, 27887005, 167, 0, 164),
    SKIN_OF_MEDIAL_PART_OF_DORSUM_OF_FOOT(CodingScheme.DCM, 130313, 421, 0, 420),
    SKIN_OF_MEDIAL_PART_OF_HEEL(CodingScheme.SCT, 699919007, 445, 0, 444),
    SKIN_OF_MEDIAL_SURFACE_OF_THIGH(CodingScheme.SCT, 73958006, 401, 0, 400),
    SKIN_OF_MID_BACK(CodingScheme.DCM, 130323, 227, 0, 226),
    SKIN_OF_MID_PARASPINAL_REGION(CodingScheme.DCM, 130303, 0, 234, 0),
    SKIN_OF_NASOLABIAL_FOLD(CodingScheme.SCT, 37108007, 27, 0, 28),
    SKIN_OF_NECK(CodingScheme.SCT, 43081002, 0, 0, 0),
    SKIN_OF_NIPPLE(CodingScheme.SCT, 54468004, 205, 0, 204),
    SKIN_OF_NOSE(CodingScheme.SCT, 113179006, 0, 0, 0),
    SKIN_OF_NUCHAL_REGION(CodingScheme.SCT, 4658004, 45, 0, 46),
    SKIN_OF_OCCIPITAL_REGION(CodingScheme.SCT, 79951008, 1, 61, 2),
    SKIN_OF_PALM_OF_HAND(CodingScheme.SCT, 70887009, 341, 0, 340),
    SKIN_OF_PALMAR_AREA_OF_WRIST(CodingScheme.SCT, 24527008, 337, 0, 336),
    SKIN_OF_PALMAR_PART_OF_INDEX_FINGER(CodingScheme.FMA, 38344, 347, 0, 346),
    SKIN_OF_PALMAR_PART_OF_LITTLE_FINGER(CodingScheme.FMA, 38357, 353, 0, 352),
    SKIN_OF_PALMAR_PART_OF_MIDDLE_FINGER(CodingScheme.FMA, 38347, 349, 0, 348),
    SKIN_OF_PALMAR_PART_OF_RING_FINGER(CodingScheme.FMA, 38354, 351, 0, 350),
    SKIN_OF_PALMAR_PART_OF_THUMB(CodingScheme.FMA, 38341, 345, 0, 344),
    SKIN_OF_PARANASAL_CHEEK(CodingScheme.DCM, 130314, 15, 0, 16),
    SKIN_OF_PARASPINAL_AREA_OF_THE_NECK(CodingScheme.DCM, 130300, 0, 62, 0),
    SKIN_OF_PARASPINAL_AREA_OF_THE_SUPERIOR_BACK(CodingScheme.DCM, 130301, 0, 63, 0),
    SKIN_OF_PARIETAL_REGION(CodingScheme.SCT, 21672008, 3, 0, 4),
    SKIN_OF_PART_OF_DORSAL_SURFACE_OF_HAND(CodingScheme.SCT, 281642007, 315, 0, 314),
    SKIN_OF_PENIS(CodingScheme.SCT, 35900000, 0, 0, 0),
    SKIN_OF_PERINEUM(CodingScheme.SCT, 48014002, 0, 510, 0),
    SKIN_OF_PERIORAL_REGION_OF_FACE(CodingScheme.SCT, 110488009, 0, 0, 0),
    SKIN_OF_PHILTRUM(CodingScheme.SCT, 84365009, 0, 55, 0),
    SKIN_OF_PLANTAR_PART_OF_FIFTH_TOE(CodingScheme.FMA, 38119, 455, 0, 454),
    SKIN_OF_PLANTAR_PART_OF_FOURTH_TOE(CodingScheme.FMA, 38116, 453, 0, 452),
    SKIN_OF_PLANTAR_PART_OF_GREAT_TOE(CodingScheme.FMA, 38107, 447, 0, 446),
    SKIN_OF_PLANTAR_PART_OF_SECOND_TOE(CodingScheme.FMA, 38110, 449, 0, 448),
    SKIN_OF_PLANTAR_PART_OF_THIRD_TOE(CodingScheme.FMA, 38113, 451, 0, 450),
    SKIN_OF_POPLITEAL_FOSSA(CodingScheme.SCT, 84507004, 411, 0, 410),
    SKIN_OF_POSTAURICULAR_REGION(CodingScheme.SCT, 24483006, 9, 0, 10),
    SKIN_OF_POSTERIOR_HELIX_OF_EAR(CodingScheme.DCM, 130315, 135, 0, 134),
    SKIN_OF_POSTERIOR_LOBULE_OF_THE_EAR(CodingScheme.DCM, 130316, 141, 0, 140),
    SKIN_OF_POSTERIOR_SURFACE_OF_ELBOW(CodingScheme.SCT, 181536004, 309, 0, 308),
    SKIN_OF_POSTERIOR_SURFACE_OF_FOREARM(CodingScheme.SCT, 41550009, 311, 0, 310),
    SKIN_OF_POSTERIOR_SURFACE_OF_LOWER_LEG(CodingScheme.SCT, 47224004, 413, 0, 412),
    SKIN_OF_POSTERIOR_SURFACE_OF_THIGH(CodingScheme.SCT, 4578000, 409, 0, 408),
    SKIN_OF_POSTERIOR_SURFACE_OF_THORAX(CodingScheme.SCT, 244111001, 49, 0, 50),
    SKIN_OF_POSTERIOR_SURFACE_OF_UPPER_ARM(CodingScheme.SCT, 72939005, 307, 0, 306),
    SKIN_OF_PREAURICULAR_REGION(CodingScheme.SCT, 86719006, 11, 0, 12),
    SKIN_OF_PREPUCE_OF_CLITORIS(CodingScheme.SCT, 76723005, 0, 500, 0),
    SKIN_OF_ROOT_OF_PENIS(CodingScheme.SCT, 244117002, 0, 501, 0),
    SKIN_OF_SCALP(CodingScheme.SCT, 43067004, 0, 0, 0),
    SKIN_OF_SCROTUM(CodingScheme.SCT, 81992007, 505, 0, 503),
    SKIN_OF_SHAFT_OF_PENIS(CodingScheme.SCT, 244118007, 0, 507, 0),
    SKIN_OF_SIDE_OF_NOSE(CodingScheme.SCT, 314395006, 17, 0, 18),
    SKIN_OF_SOLE_OF_FOREFOOT(CodingScheme.DCM, 130317, 457, 0, 456),
    SKIN_OF_SUBMENTAL_AREA(CodingScheme.SCT, 34926004, 0, 59, 0),
    SKIN_OF_SUPERIOR_ANTIHELIX_OF_EAR(CodingScheme.DCM, 130318, 121, 0, 120),
    SKIN_OF_SUPERIOR_POSTERIOR_HELIX_OF_EAR(CodingScheme.DCM, 130319, 133, 0, 132),
    SKIN_OF_SUPERIOR_POSTERIOR_SURFACE_OF_THE_PINNA(CodingScheme.DCM, 130320, 137, 0, 136),
    SKIN_OF_SUPRACLAVICULAR_REGION_OF_NECK(CodingScheme.SCT, 76072005, 47, 0, 48),
    SKIN_OF_TEMPORAL_REGION(CodingScheme.SCT, 16621002, 5, 0, 6),
    SKIN_OF_THENAR_REGION_OF_PALM(CodingScheme.SCT, 26795005, 339, 0, 338),
    SKIN_OF_TIP_OF_NOSE(CodingScheme.SCT, 79283007, 21, 54, 22),
    SKIN_OF_TOE(CodingScheme.SCT, 52034004, 0, 0, 0),
    SKIN_OF_TRAGUS(CodingScheme.SCT, 79502000, 127, 0, 126),
    SKIN_OF_UMBILICUS(CodingScheme.SCT, 315003, 0, 200, 0),
    SKIN_OF_UPPER_ABDOMEN(CodingScheme.SCT, 699935000, 219, 0, 218),
    SKIN_OF_UPPER_ANTIHELIX_OF_EAR(CodingScheme.DCM, 130321, 0, 0, 0),
    SKIN_OF_UPPER_EXTREMITY(CodingScheme.SCT, 371311000, 0, 0, 0),
    SKIN_OF_UPPER_EYELID(CodingScheme.SCT, 41310005, 0, 0, 104),
    SKIN_OF_UPPER_EYELID_MARGIN(CodingScheme.DCM, 130322, 105, 0, 106),
    SKIN_OF_UPPER_INNER_QUADRANT_OF_BREAST(CodingScheme.FMA, 61426, 107, 0, 210),
    SKIN_OF_UPPER_LIP(CodingScheme.SCT, 16251004, 211, 0, 30),
    SKIN_OF_UPPER_OUTER_QUADRANT_OF_LEFT_BREAST(CodingScheme.FMA, 61439, 29, 0, 208),
    SKIN_OF_UPPER_PARASPINAL_REGION(CodingScheme.DCM, 130302, 209, 0, 232),
    SKIN_OF_UPPER_TRUNK(CodingScheme.SCT, 54440003, 232, 0, 0),
    SKIN_OF_VERMILION_PROPER_OF_LOWER_LIP(CodingScheme.FMA, 312651, 0, 57, 34),
    SKIN_OF_VERMILION_PROPER_OF_UPPER_LIP(CodingScheme.FMA, 312647, 33, 56, 32),
    SKIN_OF_VERTEX_OF_SCALP(CodingScheme.SCT, 61719002, 31, 51, 0),
    STERNAL_SKIN(CodingScheme.SCT, 244107007, 0, 201, 0),
    SUBMANDIBULAR_TRIANGLE(CodingScheme.SCT, 5713008, 39, 0, 40),
    TOOTH(CodingScheme.SCT, 38199008, 0, 0, 0),
    VAGINAL_INTROITUS(CodingScheme.SCT, 18857001, 0, 506, 0),
    VULVAL_VESTIBULE(CodingScheme.SCT, 23213005, 0, 516, 0);

    private static final Map<String, SurfacePart> CODE_LOOKUP;
    private final CodingScheme scheme;
    private final String codeValue;
    private final int left;
    private final int middle;
    private final int right;

    private SurfacePart(CodingScheme scheme, int codeValue, int left, int middle, int right) {
        this.scheme = scheme;
        this.codeValue = String.valueOf(codeValue);
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    @Override
    public String getCodeValue() {
        return this.codeValue;
    }

    @Override
    public String getCodeMeaning() {
        return MesSurface.getString(this.codeValue);
    }

    public String getCodeMeaning(Locale locale) {
        return MesSurface.getString(this.codeValue, locale);
    }

    @Override
    public CodingScheme getCodingScheme() {
        return this.scheme;
    }

    @Override
    public String getLegacyCode() {
        return null;
    }

    @Override
    public boolean isPaired() {
        return this.left > 0 && this.right > 0;
    }

    public int getLeft() {
        return this.left;
    }

    public int getMiddle() {
        return this.middle;
    }

    public int getRight() {
        return this.right;
    }

    public String toString() {
        return this.getCodeMeaning();
    }

    public static SurfacePart fromCode(String code) {
        return CODE_LOOKUP.get(code);
    }

    static {
        CODE_LOOKUP = Stream.of(SurfacePart.values()).collect(Collectors.toUnmodifiableMap(SurfacePart::getCodeValue, Function.identity()));
    }
}

