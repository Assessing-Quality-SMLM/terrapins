import smlm_terrapins as tp
from smlm_terrapins import Settings

import argparse
from os import path
import sys


def run_with(working_directory: str, loc_filepath: str, hawk_loc_filepath: str) -> bool:
    settings = Settings()
    settings.set_working_directory(working_directory)
    settings.set_data_name("localisations")

    settings.set_localisations_filepath(loc_filepath)
    settings.set_hawk_localisation_filepath(hawk_loc_filepath)
    settings.set_use_localisations(True)
    loc_results = tp.assess(settings)
    if loc_results is None:
        print("Loc assessment failed")
        return False

    settings.set_data_name("images")
    data_directory = path.join(working_directory, f"localisations_data")
    recon = path.join(data_directory, "recon", "image.tiff")
    hawk_recon = path.join(data_directory, "hawk", "image.tiff")
    half_split_a = path.join(data_directory, "frc_half_split", "image", "a.tiff")
    half_split_b = path.join(data_directory, "frc_half_split", "image", "b.tiff")

    drift_split_a = path.join(data_directory, "frc_drift_split", "image", "a.tiff")
    drift_split_b = path.join(data_directory, "frc_drift_split", "image", "b.tiff")

    zip_split_a = path.join(data_directory, "frc_zip_split", "image", "a.tiff")
    zip_split_b = path.join(data_directory, "frc_zip_split", "image", "b.tiff")
    image_settings = settings.image_settings()
    image_settings.set_recon_image(recon)
    image_settings.set_hawk_recon(hawk_recon)
    image_settings.set_half_split_a(half_split_a)
    image_settings.set_half_split_b(half_split_b)
    image_settings.set_zip_split_a(zip_split_a)
    image_settings.set_zip_split_b(zip_split_b)
    image_settings.set_drift_split_a(drift_split_a)
    image_settings.set_drift_split_b(drift_split_b)
    settings.set_use_localisations(False)

    image_results = tp.assess(settings)
    if image_results is None:
        print("Image assessment failed")
        return False
    return True


def run(namespace) -> bool:
    loc_filepath = namespace.locs
    hawk_loc_filepath = namespace.hlocs
    working_directory = namespace.working_directory
    if working_directory is None:
        working_directory = "."
    if loc_filepath is None or hawk_loc_filepath is None or working_directory is None:
        return False
    return run_with(working_directory, loc_filepath, hawk_loc_filepath)

# this needs to be in main as
# imported from release scripts to run package tests
if __name__ == "__main__":
    parser = argparse.ArgumentParser(
                    prog="Tests",
                    description="Test the package using pre-built binaries or local bleeding edge versions")
    parser.add_argument("locs", help="localisations filepath")
    parser.add_argument("hlocs", help="HAWK localisations filepath")
    parser.add_argument("-wd", "--working-directory", help="Working directory for assessment")
    # parser.add_argument("-dn", "--data-name", help="Data name")
    namespace = parser.parse_args()
    if run(namespace):
        print("ok")
        sys.exit(0)
    else:
        print("fail")
        sys.exit(1)
