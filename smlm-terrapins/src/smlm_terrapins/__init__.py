from __future__ import annotations

import process
from os import path


TERRAPINS = "assessment"
DATA_NAME = "--data-name"
WORKING_DIRECTORY = "--working-directory"
SETTINGS_FILE = "--settings"
CAMERA_PIXEL_SIZE = "--camera-pixel-size-nm"
INSTRUMENT_PSF = "--instrument-psf-fwhm-nm"
MAGNIFICATION = "--magnification"
WIDEFIELD = "--widefield"
IMAGE_STACK = "--image-stack"
N_THREADS = "--n-threads"
EXTRACT = "--extract"

LOCALISATION_MODE = "localisation"
LOCALISATION_FILE = "--locs"
LOCALISATION_FILE_FORMAT = "--locs-format"
HAWK_LOCALISATION_FILE = "--locs-hawk"
HAWK_LOCALISATION_FILE_FORMAT = "--locs-hawk-format"

IMAGE_MODE = "image"
REFERENCE_IMAGE = "--reference-image"
HAWK_IMAGE = "--hawk-image"

HALF_SPLIT_IMAGE_A = "--half-split-a"
HALF_SPLIT_IMAGE_B = "--half-split-b"

DRIFT_SPLIT_IMAGE_A = "--drift-split-a"
DRIFT_SPLIT_IMAGE_B = "--drift-split-b"

ZIP_SPLIT_IMAGE_A = "--zip-split-a"
ZIP_SPLIT_IMAGE_B = "--zip-split-b"

DEFAULT_CAMERA_PIXEL_SIZE_NM = 160.0
DEFAULT_INSTRUMENT_PSF_NM = 270.0
DEFAULT_MAGNIFICATION = 10.0


class EquipmentSettings():
    def __init__(self, camera_pixel_size_nm: float, instrument_psf_nm: float):
        self.camera_pixel_size_nm_ = camera_pixel_size_nm
        self.instrument_psf_nm_ = instrument_psf_nm    

    @staticmethod
    def default() -> EquipmentSettings:
        return EquipmentSettings(DEFAULT_CAMERA_PIXEL_SIZE_NM, DEFAULT_INSTRUMENT_PSF_NM)

    def camera_pixel_size_nm(self) -> float:
        return self.camera_pixel_size_nm_
    
    def instrument_psf_nm(self) -> float:
        return self.instrument_psf_nm_


class CoreSettings():
    def __init__(self, equipment_settings: EquipmentSettings, magnification: float):
        self.equipment_settings_ = equipment_settings
        self.magnification_ = magnification

    @staticmethod
    def default() -> CoreSettings:
        return CoreSettings(EquipmentSettings.default(), DEFAULT_MAGNIFICATION)

    def equipment_settings(self) -> EquipmentSettings:
        return self.equipment_settings_

    def magnification(self) -> float:
        return self.magnification_


class SquirrelInputs():
    def __init__(self, widefield: None | str, image_stack: None | str):
        self.widefield_ = widefield
        self.image_stack_ = image_stack

    @staticmethod
    def default() -> SquirrelInputs:
        return SquirrelInputs(None, None)


class LocalisationSettings():
    def __init__(self, squirrel_inputs: SquirrelInputs, locs: None | str, hawk_locs: None | str):
        self.squirrel_inputs_ = squirrel_inputs
        self.locs_ = locs
        self.locs_format_ = "ts"
        self.hawk_locs_ = hawk_locs
        self.hawk_locs_format_ = "ts"

    @staticmethod
    def default() -> LocalisationSettings:
        return LocalisationSettings(SquirrelInputs.default(), None, None)

    def localisations_filepath(self) -> None | str:
        return self.locs_

    def set_localisations_filepath(self, filepath: str):
        self.locs_ = filepath

    def localisation_file_format(self) -> str:
        return self.locs_format_

    def set_localisations_file_format(self, format_desc: str):
        self.locs_format_ = format_desc

    def hawk_localisations_filepath(self) -> None | str:
        return self.hawk_locs_

    def set_hawk_localisation_filepath(self, filepath: str):
        self.hawk_locs_ = filepath

    def hawk_localisation_file_format(self) -> str:
        return self.hawk_locs_format_

    def set_hawk_localisations_file_format(self, format_desc: str):
        self.hawk_locs_format_ = format_desc


class JointImage():
    def __init__(self, image_a: None | str, image_b: None | str):
        self.image_a_ = image_a
        self.image_b_ = image_b

    @staticmethod
    def default() -> JointImage:
        return JointImage(None, None)

    def image_a(self) -> None | str:
        return self.image_a_

    def set_image_a(self, value: str):
        self.image_a_ = value

    def image_b(self) -> None | str:
        return self.image_b_

    def set_image_b(self, value: str):
        self.image_b_ = value


class ImageSettings():
    def __init__(self, squirrel_inputs: SquirrelInputs, 
                       recon: None | str,
                       hawk_recon: None |str,
                       half_split: JointImage,
                       drift_split: JointImage,
                       zip_split: JointImage):
        self.squirrel_inputs_ = squirrel_inputs
        self.recon_ = recon
        self.hawk_recon_ = hawk_recon
        self.half_split_ = half_split
        self.drift_split_ = drift_split
        self.zip_split_ = zip_split

    @staticmethod
    def default() -> ImageSettings:
        return ImageSettings(SquirrelInputs.default(), 
                             None, 
                             None, 
                             JointImage.default(), 
                             JointImage.default(), 
                             JointImage.default())

    def recon_image(self) -> None | str:
        return self.recon_

    def set_recon_image(self, value: str):
        self.recon_ = value

    def hawk_image(self) -> None | str:
        return self.hawk_recon_

    def set_hawk_recon(self, value: str):
        self.hawk_recon_ = value

    def half_split_a(self) -> None | str:
        return self.half_split_.image_a()

    def set_half_split_a(self, value: str):
        self.half_split_.set_image_a(value)

    def half_split_b(self) -> None | str:
        return self.half_split_.image_b()

    def set_half_split_b(self, value: str):
        self.half_split_.set_image_b(value)


    def drift_split_a(self) -> None | str:
        return self.drift_split_.image_a()

    def set_drift_split_a(self, value: str):
        self.drift_split_.set_image_a(value)

    def drift_split_b(self) -> None | str:
        return self.drift_split_.image_b()

    def set_drift_split_b(self, value: str):
        self.drift_split_.set_image_b(value)


    def zip_split_a(self) -> None | str:
        return self.zip_split_.image_a()

    def set_zip_split_a(self, value: str):
        self.zip_split_.set_image_a(value)

    def zip_split_b(self) -> None | str:
        return self.zip_split_.image_b()

    def set_zip_split_b(self, value: str):
        self.zip_split_.set_image_b(value)



class Settings():
    def __init__(self):
        self.core_settings_ = CoreSettings.default()
        self.settings_file_ = None
        self.use_localisations_ = True
        self.localisation_settings_ = LocalisationSettings.default()
        self.image_settings_ = ImageSettings.default()

    def settings_file(self) -> None | str:
        return self.settings_file_

    def core_settings(self) -> CoreSettings:
        return self.core_settings_

    def use_localisations(self) -> bool:
        return self.use_localisations_

    def set_use_localisations(self, value: bool):
        self.use_localisations_ = value

    def localisation_settings(self) -> LocalisationSettings:
        return self.localisation_settings_

    def image_settings(self) -> ImageSettings:
        return self.image_settings_

    def localisations_filepath(self) -> None | str:
        return self.localisation_settings().localisations_filepath()

    def set_localisations_filepath(self, filepath: str):
        self.localisation_settings().set_localisations_filepath(filepath)

    def localisation_file_format(self) -> str:
        return self.localisation_settings().localisation_file_format()

    def set_localisations_file_format(self, format_desc: str):
        self.localisation_settings().set_localisations_file_format(format_desc)

    def hawk_localisations_filepath(self) -> None | str:
        return self.localisation_settings().hawk_localisations_filepath()

    def set_hawk_localisation_filepath(self, filepath: str):
        self.localisation_settings().set_hawk_localisation_filepath(filepath)

    def hawk_localisation_file_format(self) -> str:
        return self.localisation_settings().hawk_localisation_file_format()

    def set_hawk_localisations_file_format(self, format_desc: str):
        self.localisation_settings().set_hawk_localisations_file_format(format_desc)


class Results():
    def __init__(self, data_directory: str):
        self.data_directory_ = data_directory


def get_commands() -> list[str]:
    return ["./resources/windows/bin/assessment.exe"]


def add_parameter(commands: list[str], key: str, value: None | str):
    if value is None:
        return
    commands.append(key)
    commands.append(value)


def add_equipment_settings_commands(commands: list[str], settings: EquipmentSettings):
    add_parameter(commands, CAMERA_PIXEL_SIZE, str(settings.camera_pixel_size_nm()))
    add_parameter(commands, INSTRUMENT_PSF, str(settings.instrument_psf_nm()))


def add_core_settings_commands(commands: list[str], settings: CoreSettings):
    add_equipment_settings_commands(commands, settings.equipment_settings())
    add_parameter(commands, MAGNIFICATION, str(settings.magnification()))


def run_assessment(arguments: list[str]) -> bool:
    return process.run_with_output(arguments, shell=False)


def assess_localisations(commands: list[str], settings: LocalisationSettings) -> None | Results:
    commands.append(LOCALISATION_MODE)
    add_parameter(commands, LOCALISATION_FILE, settings.localisations_filepath())
    add_parameter(commands, LOCALISATION_FILE_FORMAT, settings.localisation_file_format())

    add_parameter(commands, HAWK_LOCALISATION_FILE, settings.hawk_localisations_filepath())
    add_parameter(commands, HAWK_LOCALISATION_FILE_FORMAT, settings.hawk_localisation_file_format())
    if not run_assessment(commands):
        return None
    return None


def assess_images(commands: list[str], settings: ImageSettings) -> None | Results:
    commands.append(IMAGE_MODE)
    add_parameter(commands, REFERENCE_IMAGE, settings.recon_image())
    add_parameter(commands, HAWK_IMAGE, settings.hawk_image())

    add_parameter(commands, HALF_SPLIT_IMAGE_A, settings.half_split_a())
    add_parameter(commands, HALF_SPLIT_IMAGE_B, settings.half_split_b())

    add_parameter(commands, DRIFT_SPLIT_IMAGE_A, settings.drift_split_a())
    add_parameter(commands, DRIFT_SPLIT_IMAGE_B, settings.drift_split_b())

    add_parameter(commands, ZIP_SPLIT_IMAGE_A, settings.zip_split_a())
    add_parameter(commands, ZIP_SPLIT_IMAGE_B, settings.zip_split_b())
    if not run_assessment(commands):
        return None
    return None


def assess(settings: Settings) -> None | Results:
    commands = get_commands()
    commands.append(WORKING_DIRECTORY)
    commands.append("something")
    # commands.append(DATA_NAME)
    # commands.append("now")
    add_parameter(commands, SETTINGS_FILE, settings.settings_file())
    add_core_settings_commands(commands, settings.core_settings())
    commands.append(EXTRACT)
    if (settings.use_localisations()):
        return assess_localisations(commands, settings.localisation_settings())
    return assess_images(commands, settings.image_settings())
