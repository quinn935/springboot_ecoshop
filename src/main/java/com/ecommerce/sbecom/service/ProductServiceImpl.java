package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.exceptions.APIException;
import com.ecommerce.sbecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sbecom.model.Cart;
import com.ecommerce.sbecom.model.Category;
import com.ecommerce.sbecom.model.Product;
import com.ecommerce.sbecom.payload.CartDTO;
import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.payload.ProductResponse;
import com.ecommerce.sbecom.respositories.CartRepository;
import com.ecommerce.sbecom.respositories.CategoryRepository;
import com.ecommerce.sbecom.respositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private CartService cartService;

    @Value("${project.image}")
    private String path;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category", "categoryId", categoryId));

//        check whether the product name is already exists under certain category
        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();
        for(Product value : products){
            if(value.getProductName().equals(productDTO.getProductName())){
                isProductNotPresent = false;
                break;
            }
        }
        if(isProductNotPresent){
            Product product = modelMapper.map(productDTO, Product.class);
            product.setCategory(category);
            product.setImage("default.png");
            double specialPrice = product.getPrice() - (product.getDiscount() * 0.01) * product.getPrice();
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }else{
            throw new APIException("Product already exists.");
        }

    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findAll(pageDetails);
        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category", "categoryId", categoryId));
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
        List<Product> products = pageProducts.getContent();
        if(products.isEmpty()){
            throw new APIException(category.getCategoryName() + " does not have any products");
        }
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;

    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);
        List<Product> products = pageProducts.getContent();
        if(products.isEmpty()){
            throw new APIException("Products not found with keyword " + keyword);
        }
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
//        get the existing product from the database
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product", "productId", productId));
        Product product = modelMapper.map(productDTO, Product.class);
//        update the product info with user shared
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription((product.getDescription()));
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

//        save to database
        Product savedProduct = productRepository.save(productFromDb);

//        update product in cartItems
        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOS = carts.stream().map(cart ->{
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> productDTOS = cart.getCartItems().stream().map(cartItem -> {
               return modelMapper.map(cartItem.getProduct(), ProductDTO.class);
            }).toList();

            cartDTO.setProducts(productDTOS);
            return cartDTO;
        }).toList();

        cartDTOS.forEach(cartDTO -> cartService.updateProductInCarts(cartDTO.getCartId(),productId));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product", "productId", productId));
//      delete product from carts
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));
        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
//      get the product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product", "productId", productId));

//      upload image to server
//      get the file name of uploaded image
        String fileName = fileService.uploadImage(path, image);

//      updating the new file name to the product
//      sjosjso.jpg
        productFromDb.setImage(fileName);

//      save updated product
        Product updatedProduct = productRepository.save(productFromDb);

//      return DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

}
