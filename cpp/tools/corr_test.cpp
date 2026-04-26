#include "imp.hpp"
#include <opencv2/imgproc.hpp>
#include <iostream>
#include <iomanip>
#include <chrono>
#include <random>

#include <opencv2/imgcodecs.hpp> // to set imread flag
using std::cout;
using std::endl;
using std::mt19937;
using std::uniform_real_distribution;
using std::uniform_int_distribution;


template<class T> cv::Mat_<T> random_image(mt19937&);

template<>
cv::Mat_<double> random_image<double>(mt19937& eng){
    uniform_real_distribution<double> rng;
    cv::Mat_<double> im;
    im.create(100,100);
    for(auto& i:im)
        i = rng(eng);

    return im;
}

template<>
cv::Mat_<uint8_t> random_image<uint8_t>(mt19937& eng){
    uniform_int_distribution<> rng(0,1);
    cv::Mat_<uint8_t> im;
    im.create(100,100);
    for(auto& i:im)
        i = rng(eng);

    return im;
}
template<class T>
void test(const cv::Mat_<T>& im1, const cv::Mat_<T>& im2){
	cout << __PRETTY_FUNCTION__ << endl;

    auto t1 = std::chrono::system_clock::now();
    cv::Mat_<double> r1 = imp::correlation_map<T>(im1, im2, 5);

    auto t2 = std::chrono::system_clock::now();
    cout << std::chrono::duration_cast<std::chrono::milliseconds>(t2 - t1) << endl;
    
    cv::Mat_<double> r2 = imp::correlation_map_integral(im1,im2, 5);

    auto t3 = std::chrono::system_clock::now();

    cout << std::chrono::duration_cast<std::chrono::milliseconds>(t3 - t2) << endl;

    for (int r = 0; r < im1.rows; r++)
        for (auto c = 0; c < im2.cols; c++){
            double err = abs(r1(r,c)-r2(r,c));

            if(err > 1e-6)
                cout << std::setw(10) << r << " " << c << " " << std::setw(15) << err << " " << r1(r,c) << " " << r2(r,c) << " === " << (double)im1(r,c) << endl;
            //cout << (im1(r,c)) << endl;
        }

	cout << endl;
}
template<class T>
void test(mt19937& eng){
    cv::Mat_<T> im1 = random_image<T>(eng);
    cv::Mat_<T> im2 = random_image<T>(eng);
	test(im1, im2);
}



int main(){
	mt19937 eng;
   	test<double>(eng);
	test<uint8_t>(eng);

	cv::Mat_<double> im1 = cv::imread("ref_skel.tif", cv::IMREAD_UNCHANGED);
	cv::Mat_<double> im2 = cv::imread("test_skel.tif", cv::IMREAD_UNCHANGED);
	test(im1, im2);

}





